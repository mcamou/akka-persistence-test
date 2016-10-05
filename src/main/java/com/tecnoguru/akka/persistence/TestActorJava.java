package com.tecnoguru.akka.persistence;

import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import akka.persistence.*;

import java.util.LinkedList;
import java.util.List;

class TestActorJava extends UntypedPersistentActor {
    private String id;
    private int sequence = 0;
    private List<String> history = new LinkedList<>();

    public TestActorJava(String id) {
        this.id = id;
    }

    @Override
    public String persistenceId() {
        return "java" + id;
    }

    @Override
    public void onReceiveRecover(Object msg) throws Throwable {
        ReceiveBuilder
                .match(Integer.class, s -> {
                    System.out.println("restoring sequence " + id + " - " + s);
                    sequence = s;
                })
                .match(Add.class, a -> {
                    history.add(0, a.newValue);
                    System.out.println("restoring value " + id + " - " + a.newValue);
                })
                .match(SnapshotOffer.class, s -> {
                    Snapshot state = (Snapshot) s.snapshot();
                    System.out.println("received snapshot " + id + " - " + state + "(" + s.metadata() + ")");
                    sequence = state.getSequence();
                    history = state.getHistory();
                })
                .match(RecoveryCompleted.class, x -> System.out.println("recovery completed" + id))
                .matchAny(o -> System.out.println("Unexpected recovery message " + id + ": " + o))
                .build();
    }

    @Override
    public void onReceiveCommand(Object msg) throws Throwable {
        ReceiveBuilder
                .match(GetHistory.class, g -> {
                    sequence += 1;
                    System.out.println("received sequence " + id + " - " + sequence);
                    persist(sequence, x -> {
                        System.out.println("persisted sequence " + id + " - " + x);
                    });

                    sender().tell(history, self());
                })
                .match(Add.class, ev -> {
                    history.add(0, ev.getNewValue());
                    sequence += 1;
                    System.out.println("received " + id + " - " + sequence + ": " + ev);
                    if (sequence % 10 == 0) {
                        long lastSequenceNumber = lastSequenceNr();
                        System.out.println("snapshotting " + id + " - " + sequence);
                        saveSnapshot(new Snapshot(sequence, history));
                        System.out.println("deleting old events " + id + " - " + lastSequenceNumber);
                        deleteMessages(lastSequenceNumber);
                        deleteSnapshots(SnapshotSelectionCriteria.create(lastSequenceNumber, Long.MAX_VALUE));
                    } else {
                        persist(ev, x -> {
                            System.out.println("persisted " + id + " - " + sequence + ": " + x);
                        });
                    }

                })


                .match(DeleteMessagesSuccess.class, m -> System.out.println("deleted messages " + id + " - " + m.toSequenceNr()))
                .match(DeleteMessagesFailure.class, m -> System.out.println("FAILURE deleting messages " + id + " - " + m.toSequenceNr() + ": " + m.cause()))
                .match(SaveSnapshotSuccess.class, m -> System.out.println("saved snapshot " + id + " - " + m.metadata()))
                .match(SaveSnapshotFailure.class, m -> System.out.println("FAILURE deleting snapshots " + id + " - " + m.metadata() + " (" + m.cause() + ")"))
                .match(DeleteSnapshotsSuccess.class, m -> System.out.println("deleted snapshot " + id + " - " + m.criteria()))
                .match(DeleteSnapshotFailure.class, m -> System.out.println("FAILURE deleting snapshots " + id + " - " + m.metadata() + " (" + m.cause() + ")"))
                .matchAny(x -> System.out.println("Unexpected message " + id + ": " + x))
                .build();

    }


    public static class Add {

        private final String newValue;

        public Add(String newValue) {
            this.newValue = newValue;
        }

        public String getNewValue() {
            return newValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Add add = (Add) o;

            return newValue != null ? newValue.equals(add.newValue) : add.newValue == null;

        }

        @Override
        public int hashCode() {
            return newValue != null ? newValue.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Add{" +
                    "newValue='" + newValue + '\'' +
                    '}';
        }
    }

    public static class GetHistory {
    }


    public static class Snapshot {
        private final int sequence;
        private final List<String> history;

        public Snapshot(int sequence, List<String> history) {
            this.sequence = sequence;
            this.history = history;
        }

        public int getSequence() {
            return sequence;
        }

        public List<String> getHistory() {
            return history;
        }
    }

    public static Props props(String id) {
        return Props.create(TestActorJava.class, id);
    }
}
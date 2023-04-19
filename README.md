# kafka-transactions

Testing Transaction processing in Confluent Platform / Apache Kafka

## Creating the certificates and stores

- `cd` to `security` and run `create-certs.sh` from within the directory; this will create the root certificate and all the stores for both the server and the clients.

## Starting the clusters

Start both clusters using the provided `docker-compose.yaml` file:

```bash
docker-compose up
```

## Run the transaction

From the project root directory, run:

```bash
./gradlew run
```

This will start a transactional producer and create a transaction.  

The transaction will create five messages and then commit.  Before it commits, two Consumers will read the end offsets in both uncommitted and committed configurations.

You'll see something like this:

```log
13:25:29.865 [main] INFO  TransactionalProducer : *** Begin Transaction ***
13:25:29.867 [main] INFO  TransactionalProducer : *** transactional.id txn-1 ***
13:25:29.883 [kafka-producer-network-thread | producer-txn-1] WARN  NetworkClient : [Producer clientId=producer-txn-1, transactionalId=txn-1] Error while fetching metadata with correlation id 5 : {transactions-topic=LEADER_NOT_AVAILABLE}
13:25:30.005 [main] INFO  TransactionalProducer : Sent 0:330763659
13:25:30.006 [main] INFO  TransactionalProducer : Sent 1:712819371
13:25:30.006 [main] INFO  TransactionalProducer : Sent 2:720251817
13:25:30.006 [main] INFO  TransactionalProducer : Sent 3:831381575
13:25:30.006 [main] INFO  TransactionalProducer : Sent 4:596779492
13:25:30.006 [main] INFO  TransactionalProducer : *** Before Committing Transaction ***
13:25:33.226 [main] INFO  TransactionalProducer : Read Uncommitted Isolation: transactions-topic-0: 5
13:25:36.355 [main] INFO  TransactionalProducer : Read Committed Isolation: transactions-topic-0: 0
13:25:36.362 [main] INFO  TransactionalProducer : *** Committing Transaction ***
13:25:36.367 [main] INFO  TransactionalProducer : *** Committed Transaction ***
13:25:39.785 [main] INFO  TransactionalProducer : Read Uncommitted Isolation: transactions-topic-0: 6
13:25:43.199 [main] INFO  TransactionalProducer : Read Committed Isolation: transactions-topic-0: 6
```

## Tool: GetOffsetShell

```bash
docker-compose exec broker1 kafka-run-class kafka.tools.GetOffsetShell --bootstrap-server http://localhost:9091 --topic transaction-topic
```

## Dump Log Data

```bash
docker-compose exec broker1 kafka-run-class kafka.tools.DumpLogSegments --deep-iteration --print-data-log --files /var/lib/kafka/data/transaction2-topic-0/00000000000000000000.log
```

You will see:

```bash
Dumping /var/lib/kafka/data/transaction-topic-0/00000000000000000000.log
Log starting offset: 0
baseOffset: 0 lastOffset: 4 count: 5 baseSequence: 0 lastSequence: 4 producerId: 0 producerEpoch: 0 partitionLeaderEpoch: 0 isTransactional: true isControl: false deleteHorizonMs: OptionalLong.empty position: 0 CreateTime: 1681906496697 size: 146 magic: 2 compresscodec: none crc: 2067214322 isvalid: true
| offset: 0 CreateTime: 1681906496681 keySize: 1 valueSize: 9 sequence: 0 headerKeys: [] key: 0 payload: 873343783
| offset: 1 CreateTime: 1681906496695 keySize: 1 valueSize: 9 sequence: 1 headerKeys: [] key: 1 payload: 202977431
| offset: 2 CreateTime: 1681906496697 keySize: 1 valueSize: 9 sequence: 2 headerKeys: [] key: 2 payload: 680382970
| offset: 3 CreateTime: 1681906496697 keySize: 1 valueSize: 9 sequence: 3 headerKeys: [] key: 3 payload: 883525738
| offset: 4 CreateTime: 1681906496697 keySize: 1 valueSize: 9 sequence: 4 headerKeys: [] key: 4 payload: 138158768
baseOffset: 5 lastOffset: 5 count: 1 baseSequence: -1 lastSequence: -1 producerId: 0 producerEpoch: 0 partitionLeaderEpoch: 0 isTransactional: true isControl: true deleteHorizonMs: OptionalLong.empty position: 146 CreateTime: 1681906504073 size: 78 magic: 2 compresscodec: none crc: 3680477367 isvalid: true
| offset: 5 CreateTime: 1681906504073 keySize: 4 valueSize: 6 sequence: -1 headerKeys: [] endTxnMarker: COMMIT coordinatorEpoch: 0
```

## What if we don't commit explicitly?

In this case, a slightly modified version of the Producer code (`TransactionalProducerNoCommit`) starts up, creates the five messages and then quits without calling `producer.commit()`.

You'll see the `ABORT` marker when you run `DumpLogSegments`:

```bash
docker-compose exec broker1 kafka-run-class kafka.tools.DumpLogSegments --deep-iteration --print-data-log --files /var/lib/kafka/data/transaction2-topic-0/00000000000000000000.log
Dumping /var/lib/kafka/data/transaction2-topic-0/00000000000000000000.log
Log starting offset: 0
baseOffset: 0 lastOffset: 4 count: 5 baseSequence: 0 lastSequence: 4 producerId: 1 producerEpoch: 1 partitionLeaderEpoch: 0 isTransactional: true isControl: false deleteHorizonMs: OptionalLong.empty position: 0 CreateTime: 1681907428915 size: 146 magic: 2 compresscodec: none crc: 27533021 isvalid: true
| offset: 0 CreateTime: 1681907428899 keySize: 1 valueSize: 9 sequence: 0 headerKeys: [] key: 0 payload: 128063084
| offset: 1 CreateTime: 1681907428914 keySize: 1 valueSize: 9 sequence: 1 headerKeys: [] key: 1 payload: 191011774
| offset: 2 CreateTime: 1681907428915 keySize: 1 valueSize: 9 sequence: 2 headerKeys: [] key: 2 payload: 808930906
| offset: 3 CreateTime: 1681907428915 keySize: 1 valueSize: 9 sequence: 3 headerKeys: [] key: 3 payload: 722537076
| offset: 4 CreateTime: 1681907428915 keySize: 1 valueSize: 9 sequence: 4 headerKeys: [] key: 4 payload: 848240914
baseOffset: 5 lastOffset: 5 count: 1 baseSequence: -1 lastSequence: -1 producerId: 1 producerEpoch: 1 partitionLeaderEpoch: 0 isTransactional: true isControl: true deleteHorizonMs: OptionalLong.empty position: 146 CreateTime: 1681907428939 size: 78 magic: 2 compresscodec: none crc: 3624580406 isvalid: true
| offset: 5 CreateTime: 1681907428939 keySize: 4 valueSize: 6 sequence: -1 headerKeys: [] endTxnMarker: ABORT coordinatorEpoch: 0
```

## What if we don't commit but keep the producer running?

```bash
docker-compose exec broker1 kafka-run-class kafka.tools.DumpLogSegments --deep-iteration --print-data-log --files /var/lib/kafka/data/transaction4-topic-0/00000000000000000000.log
```

We will see:

```bash
 docker-compose exec broker1 kafka-run-class kafka.tools.DumpLogSegments --deep-iteration --print-data-log --files /var/lib/kafka/data/transaction4-topic-0/00000000000000000000.log
Dumping /var/lib/kafka/data/transaction4-topic-0/00000000000000000000.log
Log starting offset: 0
baseOffset: 0 lastOffset: 4 count: 5 baseSequence: 0 lastSequence: 4 producerId: 2 producerEpoch: 0 partitionLeaderEpoch: 0 isTransactional: true isControl: false deleteHorizonMs: OptionalLong.empty position: 0 CreateTime: 1681907911396 size: 145 magic: 2 compresscodec: none crc: 152852574 isvalid: true
| offset: 0 CreateTime: 1681907911383 keySize: 1 valueSize: 9 sequence: 0 headerKeys: [] key: 0 payload: 726433482
| offset: 1 CreateTime: 1681907911395 keySize: 1 valueSize: 9 sequence: 1 headerKeys: [] key: 1 payload: 865240583
| offset: 2 CreateTime: 1681907911396 keySize: 1 valueSize: 8 sequence: 2 headerKeys: [] key: 2 payload: 32825830
| offset: 3 CreateTime: 1681907911396 keySize: 1 valueSize: 9 sequence: 3 headerKeys: [] key: 3 payload: 851138814
| offset: 4 CreateTime: 1681907911396 keySize: 1 valueSize: 9 sequence: 4 headerKeys: [] key: 4 payload: 481200393
```
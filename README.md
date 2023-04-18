# kafka-transactions

Testing Transaction processing in Confluent Platform / Apache Kafka

## Creating the certificates and stores

- `cd` to `security` and run `create-certs.sh` from within the directory; this will create the root certificate and all the stores for both the server and the clients.

## Starting the clusters

Start both clusters using the provided `docker-compose.yaml` file:

```bash
docker-compose up
```


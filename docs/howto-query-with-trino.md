# How to Analyze MFEM data with TrinoDB
These steps outline how to use a Hive Metastore to access Parquet
data with the TrinoDB distributed query engine.

## Creating the datastore and metastore

### Install the Hive Standalone Metastore on your pushdown platform
Ensure the following packages are installed:

wget
```
cd $SWHOME
mkdir opt
tar xvfz hadoop.tgz
tar xvfz hive-standalone-metastore.tgz
```

### Create the Hive Metastore and the schema store
First, you'll need to edit the metastore-site.xml. First, unset the Thrift URI:
  <property>
    <name>metastore.thrift.uris</name>
    <value></value>
    <description>Thrift URI for the remote metastore. Used by metastore client to connect to remote metastore.</description>
  </property>

Second, disable schema validation. 
  <property>
    <name>metastore.schema.verification</name>
    <value>false</value>
  </property>

Strictly speaking, neither of these steps are required if you know exactly how you want things configured. But this will make experimentation with TrinoDB simpler and easier.

```
export HADOOP_HOME=$SWHOME/opt/hadoop_dir
export HIVE_HOME=$SWHOME/opt/hive_dir
cd $HIVE_HOME
bin/schematool --dbType derby --initSchema
bin/start-metastore

mkdir data/hive/mfem
```

Note: In some instances, there may be a conflict in the version of distributed JARfiles supplied by Hadoop and Hive. Typically, this is because your Hadoop installation is newer. The simplest solution is to determine the conflicting JARfiles (often Guava) and perform the following steps:
```
cd $HIVE_HOME/lib
mkdir obsolete
mv conficting.jar obsolete
```

### Setup a Trino captible data store (S3)
In this instace we will use the Versity S3 Gateway, but any Trino compatible datastore should work fine.

```
git clone versitygw
cd versitygw
make
./versitygw -- --
```

## Setting up a TrinoDB Analytics Installation
By running all of the data and metadata services on your baremetal node, we now need to use a container
running TrinoDB to access that data.

### Installing and configuring TrinoDB
Download the trino container and perform the following steps. The metastore IP is the IP the docker container uses to contact your host.

```
docker pull trinodb/trino:342
docker run --name trino -d -p 8080:8080 trinodb/trino
docker exec -u 0 -it trino bash
echo "connector.name=hive\nhive.metastore.uri=thrift://172.17.0.1:9083" > /etc/trino/catalogs/hive.properties
docker stop trino
docker start trino
```

### Connecting to the Parquet Dataset with TrinoDB
Connecting TrinoDB to the Hive Metastore requires several steps. First we will create the Schema on the metastore and associate it with the $HOME_HOME/data/hive/mfem directory. We will then link the Parquet file into that schema as an external table.

```
docker exec -it trino trino
create SCHEMA if not exists hive.mfem with ( location = '$HIVE_HOME/data');
exit
cd $HIVE_HOME/data/hive/mfem
mkdir $TAPROOT/data/laghos_points
cp $TAPROOT/data/1m_points/1m_points.parquet $TAPROOT/data/laghos_points/0.parq
ln -s laghos_1m_points_ext $TAPROOT/data/laghos_points
docker exec -it trino trino
create table if not exists hive.mfem.laghos_points_ext
    (elementId bigint, vertexId bigint, x double, y double, z double, e double, rho double, v_x double, v_y double, v_z double)
    with (external_location = '$HIVE_HOME/data/hive/mfem/laghos_points_ext', format = 'parquet');
select * from hive.laghos_points_ext limit 10;
```

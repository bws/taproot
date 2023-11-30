# How to Analyze MFEM data with Fastbit
These steps outline how to use a Hive Metastore to access Parquet
data with the TrinoDB distributed query engine.

## Creating the datastore and metastore

### Install mysql
sudo apt install mysql-server mysql-client

### Install fastbit and fastbit UDF
```
git clone https://github.com/fastbit.git
./configure --prefix=/opt/fastbit
make
sudo make install
```

```
git clone https://github.com/bws/Fastbit_UDF.git
make udf
sudo cp fb_udf.so /usr/lib/mysql/plugins
sudo mkdir -p /var/lib/fastbit
sudo chown mysql.mysql /var/lib/fastbit
sudo service mysql restart
mysql -e "create database fastbit"
mysql fastbit < install.sql
```

## Create and Load a CSV into Fastbit

```
duckdb -c "create table mfem_1m as select * from '1m.parquet'; copy mfem_1m to 'mfem_1m.csv' (HEADER false, DELIMITER ',')"
mysql fastbit -e "select fb_create('/var/lib/fastbit/mfem', 'ele:int,vert:int,x:double,y:double,z:double,e:double,rho:double,v_x:double,v_y:double,v_z:double')"
mysql fastbit -e "select fb_load('/var/lib/fastbit/mfem', 'ele:int,vert:int,x:double,y:double,z:double,e:double,rho:double,v_x:double,v_y:double,v_z:double')"


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

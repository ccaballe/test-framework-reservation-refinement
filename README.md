# Reservation Refinement in Mesos framework


### Overview

This framework shows how to use dynamic reservation and reservation refinement in Mesos. The framework runs an scheduler
with role _tenant1/private/test-framework_. This means that can only receive offers with _tenant1/private_ or 
_tenant1/private/test-framework_.  


### Prepare environment

Make sure you have Mesos installed. You can find more information about how to install Mesos [here](http://mesos.apache.org/documentation/latest/building/).

After you have installed Mesos, we can prepare our cluster. In this example, we run 4 agents that offers 1 cpu, 1024 MB of memory and 10000 MB of disk respectively.

`/path/to/mesos/bin/mesos-master.sh --ip=127.0.0.1 --work_dir=/tmp`

`/path/to/mesos/bin/mesos-agent.sh --master=127.0.0.1:5050 --work_dir=/tmp/a --port=5051 --containerizers=mesos,docker --resources='cpus:1;mem:1024;disk:10000'`

`/path/to/mesos/bin/mesos-agent.sh --master=127.0.0.1:5050 --work_dir=/tmp/b --port=5052 --containerizers=mesos,docker --resources='cpus:1;mem:1024;disk:10000'`

`/path/to/mesos/bin/mesos-agent.sh --master=127.0.0.1:5050 --work_dir=/tmp/c --port=5053 --containerizers=mesos,docker --resources='cpus:1;mem:1024;disk:10000'`

`/path/to/mesos/bin/mesos-agent.sh --master=127.0.0.1:5050 --work_dir=/tmp/d --port=5054 --containerizers=mesos,docker --resources='cpus:1;mem:1024;disk:10000'`

Finally, we can perform the dynamic reservations to split assign roles to agents as follows:

* Agent 0 and agent 1 has resources reserved to tenant1
* Agent 2 and agent 3 has resources reserved to tenant2
* Agent 0 has also resources reserved to tenant1/private 
* Agent 1 has also resources reserved to tenant1/public
* Agent 2 has also resources reserved to tenant2/private
* Agent 3 has also resources reserved to tenant2/public

A python script that simplify this reservations is provided:

`python scripts/reserve_cluster.py`


### Compiling and testing the framework

To compile with maven:

`mvn clean compile assembly:single`

Then, set the environment var MESOS_NATIVE_JAVA_LIBRARY with the mesos library:

`export MESOS_NATIVE_JAVA_LIBRARY=/usr/local/lib/libmesos.so`

After that, you can run the framework as follows:

`java -cp target/test-framework-1.0-SNAPSHOT-jar-with-dependencies.jar Main`

The framework will receive offers with reservations of _tenant1/private_ role. These type of offer will be accepted and 
the framework will perform a reservation refinement with role _tenant1/private/test-framework_. After that, the scheduler 
will take the offer to launch an executor that runs a task in the Agent 0. When the task is completed, the executor sends
an update of its status to finish, and the scheduler will stop the driver.


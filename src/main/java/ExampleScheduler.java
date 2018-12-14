import com.google.protobuf.ByteString;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;

import java.util.ArrayList;
import java.util.List;

public class ExampleScheduler implements Scheduler {

    private Protos.ExecutorInfo executorInfo;


    public ExampleScheduler(Protos.ExecutorInfo executorInfo) {
        this.executorInfo = executorInfo;
    }

    public void registered(SchedulerDriver schedulerDriver, Protos.FrameworkID frameworkID, Protos.MasterInfo masterInfo) {
        System.out.println("Registered");
        System.out.println(frameworkID);
        System.out.println(masterInfo);
    }

    public void reregistered(SchedulerDriver schedulerDriver, Protos.MasterInfo masterInfo) {
        System.out.println("ReRegistered");
    }

    public void resourceOffers(SchedulerDriver schedulerDriver, List<Protos.Offer> list) {

        for (Protos.Offer offer : list) {
            System.out.println("--------------- RECEIVING OFFERS ------------ \n" + offer);

            List<Protos.TaskInfo> tasks = new ArrayList<Protos.TaskInfo>();

            // Tenant can be discovered in allocation_info
            String tenant = offer.getAllocationInfo().getRole().split("/")[0];
            String typeOfNode = offer.getAllocationInfo().getRole().split("/")[1];
            String fwRole = offer.getAllocationInfo().getRole().split("/")[2];


            ArrayList<Protos.Resource> newResources = new ArrayList<Protos.Resource>();

            boolean readyToLaunch = false;

            for (Protos.Resource resource : offer.getResourcesList()) {
                List<Protos.Resource.ReservationInfo> reservationInfos = resource.getReservationsList();

                if (hasReserveInTenant(tenant, typeOfNode, reservationInfos)) {
                    System.out.println("------------------- READY TO RESERVE RESOURCES TO FRAMEWORK --------------------");
                    ArrayList<Protos.Resource.ReservationInfo> reservationInfosFw = new ArrayList<Protos.Resource.ReservationInfo>();
                    reservationInfosFw.add(Protos.Resource.ReservationInfo.newBuilder().setRole(resource.getAllocationInfo().getRole()).setType(Protos.Resource.ReservationInfo.Type.DYNAMIC).build());
                    Protos.Resource newResource = Protos.Resource.newBuilder(resource).addAllReservations(reservationInfosFw).build();
                    newResources.add(newResource);
                } else if (isReserved(tenant, typeOfNode, fwRole, reservationInfos)) {
                    System.out.println("------------------- READY TO USE THE RESERVATION --------------------");
                    readyToLaunch = true;

                } else {
                    System.out.println("------------------- UNRESERVED RESOURCES. SKIPPING --------------------");
                }
            }

            if (readyToLaunch) {
                Protos.TaskID taskId = Protos.TaskID.newBuilder().setValue("hello").build();
                System.out.println("------------------------------ LAUNCHING TASK " + taskId.getValue() + " --------------------------------------");
                Protos.TaskInfo task = Protos.TaskInfo
                        .newBuilder()
                        .setName("task " + taskId.getValue())
                        .setTaskId(taskId)
                        .setSlaveId(offer.getSlaveId())
                        .setData(ByteString.copyFromUtf8("hello executor"))
                        .addAllResources(offer.getResourcesList())
                        .setExecutor(Protos.ExecutorInfo.newBuilder(executorInfo)).build();
                tasks.add(task);
                Protos.Offer.Operation op = Protos.Offer.Operation.newBuilder().setType(Protos.Offer.Operation.Type.LAUNCH).setLaunch(Protos.Offer.Operation.Launch.newBuilder().addAllTaskInfos(tasks)).build();
                ArrayList<Protos.OfferID> offerIDS = new ArrayList<Protos.OfferID>();
                offerIDS.add(offer.getId());
                ArrayList<Protos.Offer.Operation> operationList = new ArrayList<Protos.Offer.Operation>();
                operationList.add(op);
                Protos.Filters filters = Protos.Filters.newBuilder().setRefuseSeconds(1).build();
                schedulerDriver.acceptOffers(offerIDS, operationList, filters);

                break;
            } else {
                // If there are resources, means that can perform the reservation
                if (newResources.size() > 0) {
                    System.out.println("------------------- PERFORMING RESERVATIONS --------------------");
                    Protos.Offer.Operation op = Protos.Offer.Operation.newBuilder().setType(Protos.Offer.Operation.Type.RESERVE).setReserve(Protos.Offer.Operation.Reserve.newBuilder().addAllResources(newResources).build()).build();
                    ArrayList<Protos.OfferID> offerIDS = new ArrayList<>();
                    offerIDS.add(offer.getId());
                    ArrayList<Protos.Offer.Operation> operationList = new ArrayList<>();
                    operationList.add(op);
                    Protos.Filters filters = Protos.Filters.newBuilder().setRefuseSeconds(1).build();
                    schedulerDriver.acceptOffers(offerIDS, operationList, filters);
                    break;
                }
            }

        }
    }

    private boolean isReserved(String tenant, String typeOfNode, String role, List<Protos.Resource.ReservationInfo> reservationInfos) {
        return reservationInfos.size() == 3 &&
                reservationInfos.get(0).getType() == Protos.Resource.ReservationInfo.Type.DYNAMIC &&
                reservationInfos.get(1).getType() == Protos.Resource.ReservationInfo.Type.DYNAMIC &&
                reservationInfos.get(2).getType() == Protos.Resource.ReservationInfo.Type.DYNAMIC &&
                reservationInfos.get(0).getRole().equals(tenant) &&
                reservationInfos.get(1).getRole().equals(tenant + "/" + typeOfNode) &&
                reservationInfos.get(2).getRole().equals(tenant + "/" + typeOfNode + "/" + role);
    }

    private boolean hasReserveInTenant(String tenant, String typeOfNode, List<Protos.Resource.ReservationInfo> reservationInfos) {
        return reservationInfos.size() == 2 && reservationInfos.get(0).getType() == Protos.Resource.ReservationInfo.Type.DYNAMIC &&
                reservationInfos.get(1).getType() == Protos.Resource.ReservationInfo.Type.DYNAMIC &&
                reservationInfos.get(0).getRole().equals(tenant) &&
                reservationInfos.get(1).getRole().equals(tenant + "/" + typeOfNode);
    }

    public void offerRescinded(SchedulerDriver schedulerDriver, Protos.OfferID offerID) {
        System.out.println("offerRescinded");
    }

    public void statusUpdate(SchedulerDriver schedulerDriver, Protos.TaskStatus taskStatus) {
        System.out.println("Status update: task " + taskStatus.getTaskId() +
                " is in state " + taskStatus.getState());
        if (taskStatus.getState() == Protos.TaskState.TASK_FINISHED)
            schedulerDriver.stop();
    }

    public void frameworkMessage(SchedulerDriver schedulerDriver, Protos.ExecutorID executorID, Protos.SlaveID slaveID, byte[] bytes) {
        System.out.println("frameworkMessage");
    }

    public void disconnected(SchedulerDriver schedulerDriver) {
        System.out.println("disconnected");
    }

    public void slaveLost(SchedulerDriver schedulerDriver, Protos.SlaveID slaveID) {
        System.out.println("slaveLost");
    }

    public void executorLost(SchedulerDriver schedulerDriver, Protos.ExecutorID executorID, Protos.SlaveID slaveID, int i) {
        System.out.println("executorLost");
    }

    public void error(SchedulerDriver schedulerDriver, String s) {
        System.out.println("error" + s);
    }

}

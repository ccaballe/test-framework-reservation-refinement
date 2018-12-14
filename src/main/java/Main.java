
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;


public class Main {
    static String frameworkName = "framework-example";
    static String executorName = "ExampleExecutor";
    static String path = System.getProperty("user.dir") + "/target/test-framework-1.0-SNAPSHOT-jar-with-dependencies.jar";
    static String command = "java -cp " + path + " " + executorName;


    private static Protos.FrameworkInfo getFrameworkInfo() {
        Protos.FrameworkInfo.Builder builder = Protos.FrameworkInfo.newBuilder();
        builder.setFailoverTimeout(120000);
        builder.setUser("");
        builder.setName(frameworkName);
        // Only enable multirole if role is provided
        builder.addCapabilitiesBuilder().setType(Protos.FrameworkInfo.Capability.Type.MULTI_ROLE);
        builder.addRoles("tenant1/private/test-framework");
        builder.addCapabilitiesBuilder().setType(Protos.FrameworkInfo.Capability.Type.RESERVATION_REFINEMENT);
        return builder.build();
    }

    private static Protos.CommandInfo.URI getUri() {
        Protos.CommandInfo.URI.Builder uriBuilder = Protos.CommandInfo.URI.newBuilder();
        uriBuilder.setValue(path);
        uriBuilder.setExtract(false);
        return uriBuilder.build();
    }

    private static Protos.CommandInfo getCommandInfo() {
        Protos.CommandInfo.Builder cmdInfoBuilder = Protos.CommandInfo.newBuilder();
        cmdInfoBuilder.addUris(getUri());
        cmdInfoBuilder.setValue(command);
        return cmdInfoBuilder.build();
    }

    private static Protos.ExecutorInfo getExecutorInfo() {
        Protos.ExecutorInfo.Builder builder = Protos.ExecutorInfo.newBuilder();
        builder.setExecutorId(Protos.ExecutorID.newBuilder().setValue(executorName));
        builder.setCommand(getCommandInfo());
        builder.setName(executorName);

        builder.setSource("java");
        return builder.build();
    }

    private static void runFramework(String mesosMaster) {
        Scheduler scheduler = new ExampleScheduler(getExecutorInfo());
        MesosSchedulerDriver driver = new MesosSchedulerDriver(scheduler, getFrameworkInfo(), mesosMaster);
        int status = driver.run() == Protos.Status.DRIVER_STOPPED ? 0 : 1;

        driver.stop();
        System.exit(status);
    }


    public static void main(String[] args) throws Exception {
        runFramework("127.0.0.1:5050");
    }
}

import org.apache.mesos.Executor;
import org.apache.mesos.ExecutorDriver;
import org.apache.mesos.MesosExecutorDriver;
import org.apache.mesos.Protos;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class ExampleExecutor implements Executor {


    public void registered(ExecutorDriver executorDriver, Protos.ExecutorInfo executorInfo, Protos.FrameworkInfo frameworkInfo, Protos.SlaveInfo slaveInfo) {

        System.out.println("Registered executor on " + slaveInfo);
    }

    public void reregistered(ExecutorDriver executorDriver, Protos.SlaveInfo slaveInfo) {
        System.out.println("Executor reregistered");
    }

    public void disconnected(ExecutorDriver executorDriver) {
        System.out.println("Executor disconnected");
    }

    public void launchTask(final ExecutorDriver executorDriver, final Protos.TaskInfo taskInfo) {
        System.out.println(taskInfo.getData());
        Protos.TaskStatus status = Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId())
                .setState(Protos.TaskState.TASK_RUNNING).build();
        System.out.println("Changing status to RUNNING");
        executorDriver.sendStatusUpdate(status);
        try {
            Thread thread = new Thread() {
                public void run() {
                    try {
                        runProcess("date");
                        runProcess("sleep 60");
                        System.out.println("Changing status to FINISHED");
                        executorDriver.sendStatusUpdate(Protos.TaskStatus.newBuilder().setTaskId(taskInfo.getTaskId())
                                .setState(Protos.TaskState.TASK_FINISHED).build());

                    } catch (InterruptedException v) {
                        System.out.println(v);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();

            System.out.println("Thread terminado");
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    public void killTask(ExecutorDriver executorDriver, Protos.TaskID taskID) {
        System.out.println("Executor kill task");
    }

    public void frameworkMessage(ExecutorDriver executorDriver, byte[] bytes) {
        System.out.println("Executor framework message");
    }


    /**
     * Print lines for any input stream, i.e. stdout or stderr.
     *
     * @param name
     *          the label for the input stream
     * @param ins
     *          the input stream containing the data
     */
    private void printLines(String name, InputStream ins) throws Exception {
        String line = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(ins));
        while ((line = in.readLine()) != null) {
            System.out.println(name + " " + line);
        }
    }

    /**
     * Execute a command with error logging.
     *
     * @param command
     *          the string containing the command that needs to be executed
     */
    private void runProcess(String command) throws Exception {
        System.out.println("Launching command " + command);
        Process pro = Runtime.getRuntime().exec(command);
        printLines(command + " stdout:", pro.getInputStream());
        printLines(command + " stderr:", pro.getErrorStream());
        pro.waitFor();
        System.out.println(command + " exitValue() " + pro.exitValue());
    }

    public void shutdown(ExecutorDriver executorDriver) {
        System.out.println("Executor shutdown");
    }

    public void error(ExecutorDriver executorDriver, String s) {
        System.out.println("Executor error");
    }

    public static void main(String[] args) throws Exception {
        MesosExecutorDriver driver = new MesosExecutorDriver(
                new ExampleExecutor());
        System.exit(driver.run() == Protos.Status.DRIVER_STOPPED ? 0 : 1);
    }
}


import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;

import java.text.DecimalFormat;
import java.util.*;

public class App {

    public static void main(String[] args) {

        System.out.println("Starting Terminal-Based CloudSim Lab Simulation...");

        try {

            // Step 1: Initialize CloudSim
            int numUser = 1;
            Calendar calendar = Calendar.getInstance();

            CloudSim.init(numUser, calendar, false);

            // Step 2: Create Datacenter
            Datacenter datacenter0 = createDatacenter("Datacenter_0");

            // Create Broker
            DatacenterBroker broker = new DatacenterBroker("Broker");
            int brokerId = broker.getId();

            // Step 3: Create 2 Virtual Machines
            List<Vm> vmList = new ArrayList<Vm>();

            int vmid = 0;
            int mips = 1000;
            long size = 10000;
            int ram = 512;
            long bw = 1000;
            int pesNumber = 1;

            // RUN 1: SpaceShared Scheduler
            Vm vm1 = new Vm(
                    vmid++, brokerId, mips, pesNumber,
                    ram, bw, size, "Xen",
                    new CloudletSchedulerSpaceShared()
            );

            Vm vm2 = new Vm(
                    vmid++, brokerId, mips, pesNumber,
                    ram, bw, size, "Xen",
                    new CloudletSchedulerSpaceShared()
            );

            // For RUN 2, replace both schedulers with:
            // new CloudletSchedulerTimeShared()

            vmList.add(vm1);
            vmList.add(vm2);

            broker.submitVmList(vmList);

            // Step 4: Create 4 Cloudlets
            List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();

            int cloudletId = 0;
            long length = 40000;
            long fileSize = 300;
            long outputSize = 300;

            UtilizationModel utilizationModel =
                    new UtilizationModelFull();

            for (int i = 0; i < 4; i++) {

                Cloudlet cloudlet = new Cloudlet(
                        cloudletId++,
                        length,
                        pesNumber,
                        fileSize,
                        outputSize,
                        utilizationModel,
                        utilizationModel,
                        utilizationModel
                );

                cloudlet.setUserId(brokerId);

                // Assign Cloudlets alternately to VM 0 and VM 1
                cloudlet.setVmId(i % 2);

                cloudletList.add(cloudlet);
            }

            broker.submitCloudletList(cloudletList);

            // Step 5: Start Simulation
            CloudSim.startSimulation();

            CloudSim.stopSimulation();

            // Step 6: Get Completed Cloudlets
            List<Cloudlet> receivedList =
                    broker.getCloudletReceivedList();

            // Step 7: Display Results
            printCloudletList(receivedList);

            System.out.println("\nSimulation Completed Successfully!");

        } catch (Exception e) {

            System.out.println("Simulation Error Occurred:");
            e.printStackTrace();
        }
    }

    // Create Datacenter
    private static Datacenter createDatacenter(String name)
            throws Exception {

        List<Host> hostList = new ArrayList<Host>();

        List<Pe> peList = new ArrayList<Pe>();

        // Create one CPU core with 2000 MIPS
        peList.add(
                new Pe(0, new PeProvisionerSimple(2000))
        );

        // Create Host
        Host host = new Host(
                0,
                new RamProvisionerSimple(2048),
                new BwProvisionerSimple(10000),
                1000000,
                peList,
                new VmSchedulerTimeShared(peList)
        );

        hostList.add(host);

        // Datacenter Characteristics
        DatacenterCharacteristics characteristics =
                new DatacenterCharacteristics(
                        "x86",
                        "Linux",
                        "Xen",
                        hostList,
                        10.0,
                        3.0,
                        0.05,
                        0.001,
                        0.0
                );

        return new Datacenter(
                name,
                characteristics,
                new VmAllocationPolicySimple(hostList),
                new LinkedList<Storage>(),
                0
        );
    }

    // Print Cloudlet Results
    private static void printCloudletList(List<Cloudlet> list) {

        String indent = "    ";

        System.out.println(
                "\n========== SIMULATION OUTPUT WINDOW =========="
        );

        System.out.println(
                "Cloudlet ID" + indent +
                "STATUS" + indent +
                "VM ID" + indent +
                "Time Taken" + indent +
                "Start Time" + indent +
                "Finish Time"
        );

        DecimalFormat dft = new DecimalFormat("###.##");

        for (Cloudlet cloudlet : list) {

            System.out.print(
                    indent + cloudlet.getCloudletId() +
                    indent + indent
            );

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {

                System.out.println(
                        "SUCCESS" + indent + indent +
                        cloudlet.getVmId() + indent + indent +
                        dft.format(cloudlet.getActualCPUTime()) +
                        indent + indent +
                        dft.format(cloudlet.getExecStartTime()) +
                        indent + indent +
                        dft.format(cloudlet.getFinishTime())
                );

            } else {

                System.out.println(
                        "FAILED"
                );
            }
        }
    }
}

package org.example;

import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class JDIStackTraceMonitor {
    public static void main(String[] args) throws IOException, IllegalConnectorArgumentsException, InterruptedException, IncompatibleThreadStateException {
        String host = "localhost";
        String port = "5005"; // Replace with the port your application is listening on

        VirtualMachine vm = attachToVM(host, port);
        EventRequestManager erm = vm.eventRequestManager();

        // Create method entry and exit requests
        MethodEntryRequest methodEntryRequest = erm.createMethodEntryRequest();
        methodEntryRequest.addClassFilter("sun.nio.fs.UnixFileSystemProvider");
        methodEntryRequest.addThreadFilter(findMainThread(vm));
        methodEntryRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
        methodEntryRequest.enable();

        EventQueue queue = vm.eventQueue();
        while (true) {
            EventSet eventSet = queue.remove();
            EventIterator eventIterator = eventSet.eventIterator();
            while (eventIterator.hasNext()) {
                Event event = eventIterator.nextEvent();
                if (event instanceof MethodEntryEvent) {
                    MethodEntryEvent methodEntryEvent = (MethodEntryEvent) event;
                    ((MethodEntryEvent) event).thread().frames().getLast();
                    System.out.println(event);
                } else if (event instanceof MethodExitEvent) {
                    MethodExitEvent methodExitEvent = (MethodExitEvent) event;
                } else if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
                }
            }
            eventSet.resume();
        }
    }

    private static ThreadReference findMainThread(VirtualMachine vm) {
        for (ThreadReference thread : vm.allThreads()) {
            if (thread.name().equals("main")) {
                return thread;
            }
        }
        throw new IllegalStateException("Main thread not found");
    }

    private static VirtualMachine attachToVM(String host, String port) throws IOException, IllegalConnectorArgumentsException {
        AttachingConnector connector = null;
        for (AttachingConnector ac : Bootstrap.virtualMachineManager().attachingConnectors()) {
            if (ac.name().equals("com.sun.jdi.SocketAttach")) {
                connector = ac;
                break;
            }
        }
        if (connector == null) {
            throw new IllegalStateException("Cannot find SocketAttach connector");
        }

        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        arguments.get("hostname").setValue(host);
        arguments.get("port").setValue(port);

        return connector.attach(arguments);
    }
}


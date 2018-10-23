package edu.cmu.inmind.services.muf.components.osgi;

import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.yahoo.inmind.launchpad.core.framework.FelixFramework;
import edu.cmu.yahoo.inmind.launchpad.core.repos.RepositoryManager;
import edu.cmu.yahoo.inmind.launchpad.java.binders.LaunchpadContextJavaBinder;
import edu.cmu.yahoo.inmind.launchpad.utils.LaunchpadContext;
import edu.cmu.yahoo.inmind.launchpad.utils.LoggerContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.felix.framework.Felix;
import org.osgi.framework.Bundle;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.resource.Capability;

import static edu.cmu.inmind.services.muf.components.osgi.LaunchpadConstants.FELIX_CMD_BUNDLE_LEVEL;
import static edu.cmu.inmind.services.muf.components.osgi.LaunchpadConstants.FELIX_CMD_START_LEVEL;
import static edu.cmu.inmind.services.muf.components.osgi.LaunchpadConstants.FRAMEWORK_ALREADY_INIT;
import static edu.cmu.inmind.services.muf.components.osgi.LaunchpadConstants.FRAMEWORK_NOT_INITIALIZED;
import static edu.cmu.inmind.services.muf.components.osgi.LaunchpadConstants.OBR_PREFIX;
import static edu.cmu.inmind.services.muf.components.osgi.LaunchpadConstants.OSGI_EE;
import static edu.cmu.inmind.services.muf.components.osgi.LaunchpadConstants.REPOSITORY_SUFFIX;
import static org.apache.felix.bundlerepository.impl.RepositoryAdminImpl.REPOSITORY_URL_PROP;

/**
 * Created by adangi.
 */
public class LaunchpadStarter {

    private static final String TAG = LaunchpadStarter.class.getSimpleName();
    private static LoggerContext logger;
    private static LaunchpadStarter instance;
    private FelixFramework felixFramework;
    private Map<String, String> remoteServerObrsMap;
    private Map<ServiceReference, Object> serviceImplMap;

    private LaunchpadStarter() {
        Log4J.info(this, "Creating LaunchpadStarter");

        // bind this Android application's context
        // to the LaunchpadContext via the LaunchpadContextJavaBinder
        LaunchpadContextJavaBinder.bind();

        // obtain the logger from the launchpad context
        logger = LaunchpadContext.getLoggerContext();

        // if felix framework is already initialized
        validateFelixFrameworkInitialized();

        // Initialize felix framework
        felixFramework = FelixFramework.getInstance();
        remoteServerObrsMap = new HashMap<>();
        serviceImplMap = new HashMap<>();

        // Start the felix framework
        felixFramework.start();

        // initialize
        initialize();
        Log4J.info(this, "LaunchpadStarter created.");
    }

    public static LaunchpadStarter getInstance() {
        if (instance == null) {
            instance = new LaunchpadStarter();
        }
        return instance;
    }

    public ServiceReference[] getAllServices (boolean registeredOnly) {
        if (felixFramework == null) throw new RuntimeException(FRAMEWORK_NOT_INITIALIZED);
        if (registeredOnly) {
            return felixFramework.getRegisteredServices();
        }
        return felixFramework.getAllServices();
    }

    public ServiceReference[] getAllServices () {
        return getAllServices(Boolean.FALSE);
    }

    public Map<ServiceReference, Object> getAllServiceImplementations() {
        ServiceReference[] allServiceReferences = getAllServices();
        for (ServiceReference serviceReference : allServiceReferences) {
            serviceImplMap.put(serviceReference, felixFramework.getBundleContext().getService(serviceReference));
        }
        return serviceImplMap;
    }

    public String getImplementation(String apiName) {
        if (felixFramework == null) throw new RuntimeException(FRAMEWORK_NOT_INITIALIZED);
        ServiceReference sref = felixFramework.getBundleContext().getServiceReference(apiName);
        return felixFramework.getBundleContext().getService(sref).getClass().getName();
    }

    public Object getImplementation(ServiceReference serviceReference) {
        if (serviceReference != null) {
            return felixFramework.getBundleContext().getService(serviceReference);
        }
        throw new RuntimeException("Service implementation not found for " + serviceReference);
    }

    public ServiceReference[] getImplementations(String apiName, String filter) {
        if (felixFramework == null) throw new RuntimeException(FRAMEWORK_NOT_INITIALIZED);

        try {
            return felixFramework.getBundleContext().getServiceReferences(apiName, filter);
        } catch (InvalidSyntaxException ise) {
            throw new RuntimeException(ise);
        }
    }

    private void initialize () {

        // print osgi.ee capabilities to ensure that the system capabilities have been loaded
        printOSGiCapabilities();

        // get the number of bundles and print them
        felixFramework.getBundleManager().size();
        felixFramework.getBundleManager().listBundles();

        // list services available with felix framework
        felixFramework.listRegisteredServices();

        // initialize the repositories
        // initRemoteServerObrs();
        initRepositories();
    }

    public void stopFelixFramework() {

        // if felix framework is not initialized
        if (felixFramework == null) return;

        // stop the framework
        felixFramework.stop();
        felixFramework = null;
    }

    public static boolean bundleHasServices(ServiceReference[] serviceReferences) {
        return (serviceReferences != null && serviceReferences.length > 0);
    }

    public ServiceReference[] getServices(String bundleName, boolean preprocess) throws InvalidSyntaxException {
        Bundle bundle = getBundle(bundleName);
        ServiceReference[] bundleServices = bundle.getRegisteredServices();
        if (bundleHasServices(bundleServices)) {
            System.out.println("Bundle: " + bundle + " has " + bundleServices.length + " services: " + Arrays.toString(bundleServices));
        }
        else {
            System.out.println("Bundle: " + bundle + " has no services.");
        }
        return bundleServices;
    }

    public Bundle getBundle(String bundleName) {
        Bundle[] bundles = felixFramework.getBundleContext().getBundles();
        for (Bundle bundle : bundles) {
            if (bundle.getSymbolicName().equals(bundleName)) { return bundle; }
        }
        throw new RuntimeException("No bundle found for " + bundleName);
    }

    public ServiceReference lookupService (String className) throws InvalidSyntaxException {

        // obtain the service and define which implementation to use
        String name = className.substring(className.lastIndexOf("." ));
        String filterType = name + "Type";
        String filter = "("+ filterType + "="  + name + ")";
        ServiceReference[] refs = felixFramework.getFelixInstance().getBundleContext().getServiceReferences(name, filter);

        // when service not found
        if (refs == null) {
            throw new RuntimeException("Service not registered: " + name);
        }

        // return the first obtained service reference
        return refs[0];
    }

    private void validateFelixFrameworkInitialized() {
        if (felixFramework != null) {
            throw new RuntimeException(FRAMEWORK_ALREADY_INIT);
        }
    }

    /**
     * Initializes the initial set of repositories
     * from which the Felix framework could load bundles.
     *
     * Note: <code>initRemoteServerOBRs()</code> should be called before this method usage.
     */
    private void initRepositories() {

        // obtain an instance of the repository manager
        RepositoryManager repositoryManager = felixFramework.getRepositoryManager();

        // refresh all repositories: remove previous and add the initial repositories to load
        repositoryManager.refreshRepositories(getInitialRepositories());
        repositoryManager.listRepositories();
    }

    /**
     * Returns a list of repositories that shall be loaded
     * when the Felix framework is started.
     *
     * @return a <code>List</code> of repository URLs.
     */
    private List<String> getInitialRepositories() {
        List<String> repositories = new ArrayList<>();

        // add the OBRs as mentioned in the Felix config file
        repositories.add(felixFramework.getFelixConfiguration().get(REPOSITORY_URL_PROP));

        if (remoteServerObrsMap.isEmpty()) return repositories;

        // add the custom OBRs hosted on the remote server
        for (String remoteServerObr : remoteServerObrsMap.values()) {
            repositories.add(remoteServerObr);
        }

        return repositories;
    }

    /**
     * Prints the list of system capabilities available to the Felix framework.
     */
    private void printOSGiCapabilities() {
        logger.d(TAG, OSGI_EE + " capabilities: " + getOSGiCapabilities());
    }

    /**
     * Returns a <code>List</code> of <code>osgi.ee</code> capabilities
     * that reflect the system capabilities available to the Felix framework.
     *
     * @return <code>List</code> of <code>osgi.ee</code> capabilities.
     */
    private List<Capability> getOSGiCapabilities() {
        Felix felix = felixFramework.getFelixInstance();
        Bundle bundle = felix.getBundleContext().getBundle(0);
        BundleRevision bundleRevision = bundle.adapt(BundleRevision.class);
        return bundleRevision.getCapabilities(OSGI_EE);
    }

    private void switchFrameworkLevel(int toLevel) {
        String level = Integer.toString(toLevel);
        executeCommand(String.format("%s -i %s", FELIX_CMD_BUNDLE_LEVEL, level));
        executeCommand(String.format("%s %s", FELIX_CMD_START_LEVEL, level));
        logger.d(TAG, "Felix Framework layer switched to: " + level);
    }

    /*
    public void startService(String serviceName, int atLevel) {
        String obrOsgiCore = remoteServerObrs.get(0);
        startService(obrOsgiCore, serviceName, atLevel);
    }
    */

    private String constructRemoteServerOBR(String hostOBR) {
        return (OBR_PREFIX + hostOBR + REPOSITORY_SUFFIX);
    }

    public void startService(String hostOBR, String serviceName, int atLevel) {
        String remoteServerObr;

        // if the hostOBR doesn't exist in the obr cache
        if (!remoteServerObrsMap.containsKey(hostOBR)) {

            // construct the entire repository.xml file path
            String remoteServerObrName = constructRemoteServerOBR(hostOBR);

            // obtain the remote path, that is, with the hosted server details
            remoteServerObr = RepositoryManager.getRemoteServerOBR(remoteServerObrName);

            // add it to the obr cache
            remoteServerObrsMap.put(hostOBR, remoteServerObr);

            // refresh the felix repository by adding the obr
            felixFramework.getRepositoryManager().refreshRepository(remoteServerObr);
        }
        else {
            remoteServerObr = remoteServerObrsMap.get(hostOBR);
        }

        // switch Felix framework to the desired level
        switchFrameworkLevel(atLevel);

        // start the service resource from the given OBR
        felixFramework.startResource(remoteServerObr, serviceName);
    }

    public void executeCommand(String command) {

        // execute the given command on the Felix framework
        felixFramework.executeOnShell(command);
    }

    /**
     * Initializes the initial list of OBRs hosted on the remote server
     * from which the Felix framework could load bundles.
     */
    /*
    private void initRemoteServerObrs() {
        remoteServerObrs = new ArrayList<>();
        remoteServerObrs.add(RepositoryManager.getRemoteServerOBR(URL_OBR_INMIND_OSGI_CORE));
        remoteServerObrs.add(RepositoryManager.getRemoteServerOBR(URL_OBR_INMIND_MUF));
        //remoteServerObrs.add(RepositoryManager.getRemoteServerOBR(URL_OBR_INMIND));
    }
    */

    /*
    public void startServices() {
        String obrOsgiCore = remoteServerObrs.get(0);
        final String osgiGroup = "edu.cmu.inmind.osgi.merge-Apr25.";
        startService(obrOsgiCore,osgiGroup + "bundle-commons", BASE.level());
        startService(obrOsgiCore,osgiGroup + "osgi-core", SDK.level());
        startService(obrOsgiCore,osgiGroup + "bundle-apis", SDK.level());
        startService(obrOsgiCore, "microbundle", THIRD_PARTY_LIBS.level());
        //startService(obrOsgiCore, osgiGroup + "gmail-provider", IMPLEMENTATIONS.level());
        startService(obrOsgiCore, osgiGroup + "calculator-integer", IMPLEMENTATIONS.level());
        //startService(obrOsgiCore, osgiGroup + "calculator-client", CLIENTS.level());
    }
    */

}


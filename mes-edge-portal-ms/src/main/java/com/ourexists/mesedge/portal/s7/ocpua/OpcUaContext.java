package com.ourexists.mesedge.portal.s7.ocpua;

import com.alibaba.fastjson.JSON;
import com.ourexists.era.framework.core.exceptions.BusinessException;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.stack.client.security.DefaultClientCertificateValidator;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

@Slf4j
@Component
public class OpcUaContext {

    private final ConcurrentHashMap<String, OpcUaClient> clientMap;

    private final ExecutorService executor;

    private DefaultTrustListManager trustListManager;

    public OpcUaContext() {
        this.clientMap = new ConcurrentHashMap<>();
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void createClient(String severName, String host, int port, String suffix) throws Exception {
        OpcUaClient client = clientMap.get(severName);
        if (client == null) {
            String endPointUrl = "opc.tcp://" + host + ":" + port;
            if (StringUtils.isNotBlank(suffix)) {
                endPointUrl += suffix;
            }
//            KeyPair keyPair;
//            X509Certificate x509Certificate;
//            try {
//                keyPair = CertificateReader.loadKeyPair("config/client_private.pem", "config/client_cert.pem");
//                x509Certificate = CertificateReader.loadCertificate("config/PLC-1OPCUA-1-4.cer");
//            } catch (Exception e) {
//                log.error(e.getMessage(), e);
//                throw new BusinessException("key pair load error");
//            }
            Path securityTempDir = Paths.get(System.getProperty("java.io.tmpdir"), "client", "security");
            Files.createDirectories(securityTempDir);
            if (!Files.exists(securityTempDir)) {
                throw new Exception("unable to create security dir: " + securityTempDir);
            }
            File pkiDir = securityTempDir.resolve("pki").toFile();

            trustListManager = new DefaultTrustListManager(pkiDir);
            DefaultClientCertificateValidator certificateValidator =
                    new DefaultClientCertificateValidator(trustListManager);

            KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);
            client = OpcUaClient.create(
                    endPointUrl,
                    endpoints ->
                            endpoints.stream()
                                    .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getUri()))
                                    .findFirst(),
                    configBuilder ->
                            configBuilder
                                    .setApplicationName(LocalizedText.english(KeyStoreLoader.APPLICATION_NAME))
                                    .setApplicationUri(KeyStoreLoader.APPLICATION_URI)
                                    .setKeyPair(loader.getClientKeyPair())
                                    .setCertificate(loader.getClientCertificate())
                                    .setCertificateChain(loader.getClientCertificateChain())
                                    .setCertificateValidator(certificateValidator)
                                    .setIdentityProvider(new AnonymousProvider())
                                    .setRequestTimeout(uint(5000))
                                    .build());
            clientMap.put(severName, client);
        }
    }


    public Object readNodeValue(String severName, int namespaceIndex, String identifier) {
        OpcUaClient client = clientMap.get(severName);
        if (client == null) {
            throw new BusinessException("No OpcUaClient connect for sever " + severName);
        }
        try {
            NodeId nodeId;
            try {
                int it = Integer.parseInt(identifier);
                nodeId = new NodeId(namespaceIndex, it);
            } catch (NumberFormatException e) {
                //不操作
                nodeId = new NodeId(namespaceIndex, identifier);
            }
            client.connect().get();
            UaVariableNode node = client.getAddressSpace().getVariableNode(nodeId);
            // Read the current value
            DataValue value = node.readValue();

            if (value == null || value.getStatusCode() == null || value.getStatusCode().isBad()) {
                throw new BusinessException("Error reading node value: " + JSON.toJSONString(value));
            }
            Variant variant = value.getValue();
            return variant.getValue();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        } finally {
            try {
                client.disconnect().get();
            } catch (InterruptedException | ExecutionException e) {
                //
            }
        }
    }

    /**
     * @param severName      服务端名称代号
     * @param namespaceIndex 命名空间URI的索引。索引0用于OPC UA定义的NodeId
     * @param identifier     OPC UA服务器地址空间中节点的标识符
     * @param value          写入的数据
     * @throws Exception
     */
    public void writeNodeValue(String severName, int namespaceIndex, String identifier, Object value) throws Exception {
        OpcUaClient client = clientMap.get(severName);
        if (client == null) {
            throw new BusinessException("No OpcUaClient connect for sever " + severName);
        }
        try {
            client.connect().get();
            NodeId nodeId;
            try {
                int it = Integer.parseInt(identifier);
                nodeId = new NodeId(namespaceIndex, it);
            } catch (NumberFormatException e) {
                //不操作
                nodeId = new NodeId(namespaceIndex, identifier);
            }
            UaVariableNode node = client.getAddressSpace().getVariableNode(nodeId);
            node.writeValue(new Variant(value));
        } finally {
            try {
                client.disconnect().get();
            } catch (InterruptedException | ExecutionException e) {
                //
            }
        }
    }

    public void writeNodeValues(String severName, List<NodeData> nodeDatas) {
        OpcUaClient client = clientMap.get(severName);
        if (client == null) {
            throw new BusinessException("No OpcUaClient connect for sever " + severName);
        }
        try {
            List<NodeId> nodeIds = new ArrayList<>();
            List<DataValue> dataValues = new ArrayList<>();
            for (NodeData nodeData : nodeDatas) {
                NodeId nodeId;
                try {
                    int it = Integer.parseInt(nodeData.getIdentifier());
                    nodeId = new NodeId(nodeData.getNamespaceIndex(), it);
                } catch (NumberFormatException e) {
                    nodeId = new NodeId(nodeData.getNamespaceIndex(), nodeData.getIdentifier());
                }
                nodeIds.add(nodeId);
                dataValues.add(new DataValue(new Variant(nodeData.getValue())));
            }
            client.connect().get();
            List<StatusCode> statusCodes = client.writeValues(nodeIds, dataValues).get();
            for (StatusCode statusCode : statusCodes) {
                if (statusCode.isBad()) {
                    throw new BusinessException("OpcUaClient Batch Bad status code: " + statusCode);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        } finally {
            try {
                client.disconnect().get();
            } catch (InterruptedException | ExecutionException e) {
                //
            }
        }
    }

    public void subscribe(String severName, OpcUaListener opcUaListener) {
        executor.submit(() -> {
            if (CollectionUtil.isBlank(opcUaListener.getIdentifiers())) {
                log.error("OpcUaListener identifier is empty");
                return;
            }
            OpcUaClient client = clientMap.get(severName);
            try {

                ManagedSubscription subscription = ManagedSubscription.create(client);
                List<NodeId> nodeIds = new ArrayList<>();
                for (String identifier : opcUaListener.getIdentifiers()) {
                    nodeIds.add(new NodeId(opcUaListener.getNamespaceIndex(), identifier));
                }
                List<ManagedDataItem> dataItemList = subscription.createDataItems(nodeIds);
                for (ManagedDataItem managedDataItem : dataItemList) {
                    managedDataItem.addDataValueListener((t) -> {
                        if (t == null || t.getStatusCode() == null) {
                            log.error("Node [{}] returned null", managedDataItem.getNodeId().getIdentifier().toString());
                            return;
                        }
                        if (t.getStatusCode().isBad()) {
                            log.error("Node [{}] returned error [{}]", managedDataItem.getNodeId().getIdentifier().toString(), t.getStatusCode().getValue());
                            return;
                        }
                        opcUaListener.handleData(managedDataItem.getNodeId().getIdentifier().toString(), t.getValue().getValue());
                    });
                }
                //保证线程不会被销毁回收
                final CountDownLatch eventLatch = new CountDownLatch(1);
                eventLatch.await();
            } catch (UaException e) {
                log.error("create subscribe failed", e);
            } catch (InterruptedException e) {
                log.error("thread await failed", e);
            }
        });
    }


    @Getter
    public abstract static class OpcUaListener {

        protected Integer namespaceIndex = 0;

        /**
         * 所有监听的节点
         */
        protected final List<String> identifiers;

        public OpcUaListener(Integer namespaceIndex, String identifier) {
            this(namespaceIndex, Collections.singletonList(identifier));
        }

        public OpcUaListener(Integer namespaceIndex, List<String> identifiers) {
            this.namespaceIndex = namespaceIndex;
            this.identifiers = identifiers;
        }

        /**
         * @param identifier 当前的节点
         * @param value      监听到的节点值
         */
        public abstract void handleData(String identifier, Object value);

    }
}

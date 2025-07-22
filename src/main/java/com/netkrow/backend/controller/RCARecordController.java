package com.netkrow.backend.controller;

import com.netkrow.backend.model.RCARecord;
import com.netkrow.backend.service.RCARecordService;
import com.netkrow.backend.dto.QueryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/rca")
public class RCARecordController {
    @Autowired
    private RCARecordService service;

    @Value("${oracle.datasource.url}")
    private String oracleUrl;

    @Value("${oracle.datasource.username}")
    private String oracleUser;

    @Value("${oracle.datasource.password}")
    private String oraclePassword;

    @PostMapping
    public RCARecord create(@RequestBody RCARecord r) {
        return service.save(r);
    }

    @GetMapping("/search")
    public List<RCARecord> search(@RequestParam String q) {
        return service.search(q);
    }

    @GetMapping
    public List<RCARecord> listAll() {
        return service.listAll();
    }

    @PostMapping("/query")
    public List<Map<String, Object>> executeOracleQuery(@RequestBody QueryRequest req) throws SQLException {
        String sql = req.getSql().trim();
        if (!sql.toLowerCase().startsWith("select")) {
            throw new IllegalArgumentException("Solo se permiten queries SELECT");
        }
        int maxRows = 50;
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(oracleUrl, oracleUser, oraclePassword);
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(120);
            stmt.setMaxRows(maxRows);

            try (ResultSet rs = stmt.executeQuery(sql)) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("SQL ERROR: " + e.getMessage(), e);
        }
        return result;
    }

    // --- BACKTRACE ---
    @PostMapping("/backtrace")
    public Map<String, Object> backtrace(@RequestBody Map<String, String> req) {
        String search = req.get("search");
        if (search == null || search.trim().isEmpty())
            throw new IllegalArgumentException("Debe enviar el campo 'search'.");
        int maxDepth = 15;
        if (req.containsKey("maxDepth")) {
            try { maxDepth = Integer.parseInt(req.get("maxDepth")); } catch (Exception ignore) {}
        }
        int maxRoutes = 60;

        List<List<Map<String, Object>>> allRoutes = new ArrayList<>();
        List<Map<String, Object>> allTxDetails = new ArrayList<>(); // Para mostrar info de transacción

        try (Connection conn = DriverManager.getConnection(oracleUrl, oracleUser, oraclePassword)) {
            // Busca todos los sub_flows donde aparece el servicio buscado
            String sql = "SELECT SUB_FLOW_KEY, SERVER_KEY, FLOW_KEY FROM uatconf.yfs_sub_flow WHERE DBMS_LOB.INSTR(config_xml, ?) > 0 ORDER BY modifyts DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, search.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    Set<String> visited = new HashSet<>();
                    boolean foundAny = false;
                    while (rs.next()) {
                        foundAny = true;
                        if (allRoutes.size() >= maxRoutes) break;
                        String subFlowKey = rs.getString("SUB_FLOW_KEY");
                        String serverKey = rs.getString("SERVER_KEY");
                        String flowKey = rs.getString("FLOW_KEY");
                        // Recursivamente encuentra rutas padres y las agrega
                        findAllParentRoutesFull(
                                search.trim(),
                                flowKey,
                                serverKey,
                                subFlowKey,
                                new LinkedList<>(),
                                allRoutes,
                                conn,
                                visited,
                                0,
                                maxDepth,
                                maxRoutes,
                                allTxDetails // Nuevo: para recolectar info de transacciones en cada ruta
                        );
                    }
                    // Si no hay ningún padre (caso especial: el flujo buscado es inicial), intenta buscar transacciones directas
                    if (!foundAny) {
                        List<Map<String, Object>> txNodes = findTransactionNodeDetailed(search.trim(), conn);
                        for (Map<String, Object> tx : txNodes) {
                            tx.put("type", "transaction");
                            tx.put("label", tx.get("TRANSACTION_KEY"));
                            List<Map<String, Object>> txRoute = new ArrayList<>();
                            txRoute.add(tx);
                            Map<String, Object> finalNode = new LinkedHashMap<>();
                            finalNode.put("type", "service");
                            finalNode.put("label", search.trim());
                            txRoute.add(finalNode);
                            allRoutes.add(txRoute);
                        }
                        allTxDetails.addAll(txNodes);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("SQL ERROR: " + e.getMessage(), e);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("routes", allRoutes);
        result.put("transactions", allTxDetails); // Para mostrar los datos extendidos
        return result;
    }

    /**
     * Busca todos los padres (flows) hacia atrás, agregando cada nodo al path.
     * Si encuentra un server, lo pone como origen. Si no encuentra padres, busca transacciones iniciales.
     */
    private void findAllParentRoutesFull(
            String flowToFind,
            String flowKey,
            String serverKey,
            String subFlowKey,
            LinkedList<Map<String, Object>> currentPath,
            List<List<Map<String, Object>>> allRoutes,
            Connection conn,
            Set<String> visited,
            int depth,
            int maxDepth,
            int maxRoutes,
            List<Map<String, Object>> allTxDetails
    ) throws SQLException {
        if (depth > maxDepth) return;
        if (allRoutes.size() >= maxRoutes) return;

        String nodeVisitKey = (flowKey + ":" + subFlowKey).trim();
        if (visited.contains(nodeVisitKey)) return;
        visited.add(nodeVisitKey);

        // Trae info del flow actual (puede ser null)
        Map<String, Object> flowMeta = findFlowMeta(flowKey, conn);
        String flowName = flowMeta != null ? (String) flowMeta.get("FLOW_NAME") : null;

        // --- LOGICA PARA AGREGAR FLOW INTERMEDIO ANTES DEL SERVICE ---
        if (depth == 0) {
            // Nodo service final
            Map<String, Object> step = new LinkedHashMap<>();
            step.put("type", "service");
            step.put("label", flowToFind);
            step.put("subFlowKey", subFlowKey);
            step.put("flowKey", flowKey);
            step.put("serverKey", serverKey);
            step.put("flowName", flowName);
            step.putAll(flowMeta);

            if (flowName != null && !flowName.trim().isEmpty() && !flowName.equals(flowToFind)) {
                // AGREGAR EL FLOW INTERMEDIO ANTES DEL SERVICE
                Map<String, Object> flowNode = new LinkedHashMap<>();
                flowNode.put("type", "flow");
                flowNode.put("label", flowName);
                flowNode.put("subFlowKey", subFlowKey);
                flowNode.put("flowKey", flowKey);
                flowNode.put("serverKey", serverKey);
                flowNode.put("flowName", flowName);
                flowNode.putAll(flowMeta);
                // Primero el flow intermedio, luego el service
                currentPath.addFirst(step);
                currentPath.addFirst(flowNode);
            } else {
                // Solo el service
                currentPath.addFirst(step);
            }
        } else {
            // Nodo flow intermedio
            Map<String, Object> flowNode = new LinkedHashMap<>();
            flowNode.put("type", "flow");
            flowNode.put("label", flowName);
            flowNode.put("subFlowKey", subFlowKey);
            flowNode.put("flowKey", flowKey);
            flowNode.put("serverKey", serverKey);
            flowNode.put("flowName", flowName);
            flowNode.putAll(flowMeta);
            currentPath.addFirst(flowNode);
        }

        // Si tiene SERVER_KEY válido, es el origen de la ruta
        if (serverKey != null && !serverKey.trim().isEmpty() && !serverKey.trim().equals("0")) {
            Map<String, Object> serverNode = new LinkedHashMap<>();
            serverNode.put("type", "server");
            serverNode.put("label", serverKey.trim());
            List<Map<String, Object>> finalRoute = new ArrayList<>();
            finalRoute.add(serverNode);
            finalRoute.addAll(currentPath);
            allRoutes.add(finalRoute);
            return;
        }

        // Busca padres (pueden ser varios)
        boolean foundParent = false;
        if (flowName != null && !flowName.trim().isEmpty()) {
            String sql = "SELECT SUB_FLOW_KEY, SERVER_KEY, FLOW_KEY FROM uatconf.yfs_sub_flow WHERE DBMS_LOB.INSTR(config_xml, ?) > 0 ORDER BY modifyts DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, flowName.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        foundParent = true;
                        String parentSubFlowKey = rs.getString("SUB_FLOW_KEY");
                        String parentServerKey = rs.getString("SERVER_KEY");
                        String parentFlowKey = rs.getString("FLOW_KEY");
                        // Recursivamente sigue buscando padres
                        findAllParentRoutesFull(
                                flowName.trim(),
                                parentFlowKey,
                                parentServerKey,
                                parentSubFlowKey,
                                new LinkedList<>(currentPath),
                                allRoutes,
                                conn,
                                new HashSet<>(visited),
                                depth + 1,
                                maxDepth,
                                maxRoutes,
                                allTxDetails
                        );
                    }
                }
            }
        }

        // Si no hay padres, busca transacciones asociadas como origen
        if (!foundParent) {
            List<Map<String, Object>> txNodes = findTransactionNodeDetailed(flowName != null ? flowName : flowToFind, conn);
            if (!txNodes.isEmpty()) {
                for (Map<String, Object> tx : txNodes) {
                    tx.put("type", "transaction");
                    tx.put("label", tx.get("TRANSACTION_KEY"));
                    List<Map<String, Object>> fullRoute = new ArrayList<>();
                    fullRoute.add(tx); // La transacción es el origen
                    fullRoute.addAll(currentPath); // El resto de la ruta
                    allRoutes.add(fullRoute);
                    allTxDetails.add(tx);
                    if (allRoutes.size() >= maxRoutes) return;
                }
            } else {
                // Si no hay transacción, igual agrega la ruta desde hoja a origen
                allRoutes.add(new ArrayList<>(currentPath));
            }
        }
    }

    // Busca metadatos de un flowKey (flow_name, owner, etc)
    private Map<String, Object> findFlowMeta(String flowKey, Connection conn) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        if (flowKey == null || flowKey.trim().isEmpty()) return result;
        String q2 = "SELECT FLOW_NAME, OWNER_KEY, PROCESS_TYPE_KEY, FLOW_GROUP_NAME, CREATEUSERID, CREATETS, MODIFYTS FROM uatconf.yfs_flow WHERE TRIM(flow_key) = TRIM(?)";
        try (PreparedStatement ps2 = conn.prepareStatement(q2)) {
            ps2.setString(1, flowKey.trim());
            try (ResultSet rs2 = ps2.executeQuery()) {
                if (rs2.next()) {
                    result.put("FLOW_NAME", rs2.getString("FLOW_NAME"));
                    result.put("OWNER_KEY", rs2.getString("OWNER_KEY"));
                    result.put("PROCESS_TYPE_KEY", rs2.getString("PROCESS_TYPE_KEY"));
                    result.put("FLOW_GROUP_NAME", rs2.getString("FLOW_GROUP_NAME"));
                    result.put("CREATEUSERID", rs2.getString("CREATEUSERID"));
                    result.put("CREATETS", rs2.getString("CREATETS"));
                    result.put("MODIFYTS", rs2.getString("MODIFYTS"));
                }
            }
        }
        return result;
    }

    // Busca todas las transacciones que inician el flow dado (extendido)
    private List<Map<String, Object>> findTransactionNodeDetailed(String flowName, Connection conn) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        if (flowName == null || flowName.trim().isEmpty()) return result;
        String q = "SELECT f.flow_name as FLOW_NAME, a.action_key as ACTION_KEY, a.actionname as ACTIONNAME, a.group_id as GROUP_ID, " +
                "a.createuserid as CREATEUSERID, e.transaction_key as TRANSACTION_KEY, e.eventid as EVENTID, " +
                "t.process_type_key as PROCESS_TYPE_KEY, t.owner_key as OWNER_KEY, t.listener_type as LISTENER_TYPE " +
                "FROM uatconf.yfs_action a " +
                "INNER JOIN uatconf.yfs_invoked_flows i ON a.action_key = i.action_key " +
                "INNER JOIN uatconf.yfs_flow f ON i.flow_key = f.flow_key " +
                "INNER JOIN uatconf.yfs_event_condition ec ON i.action_key = ec.action_key " +
                "INNER JOIN uatconf.yfs_event e ON ec.event_key = e.event_key " +
                "INNER JOIN uatconf.yfs_transaction t ON e.transaction_key = t.transaction_key " +
                "WHERE TRIM(f.flow_name) = TRIM(?)";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setString(1, flowName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> txNode = new LinkedHashMap<>();
                    txNode.put("FLOW_NAME", rs.getString("FLOW_NAME"));
                    txNode.put("ACTION_KEY", rs.getString("ACTION_KEY"));
                    txNode.put("ACTIONNAME", rs.getString("ACTIONNAME"));
                    txNode.put("GROUP_ID", rs.getString("GROUP_ID"));
                    txNode.put("CREATEUSERID", rs.getString("CREATEUSERID"));
                    txNode.put("TRANSACTION_KEY", rs.getString("TRANSACTION_KEY"));
                    txNode.put("EVENTID", rs.getString("EVENTID"));
                    txNode.put("PROCESS_TYPE_KEY", rs.getString("PROCESS_TYPE_KEY"));
                    txNode.put("OWNER_KEY", rs.getString("OWNER_KEY"));
                    txNode.put("LISTENER_TYPE", rs.getString("LISTENER_TYPE"));
                    // El type y label se agregan en la función que los mete en las rutas
                    result.add(txNode);
                }
            }
        }
        return result;
    }
}

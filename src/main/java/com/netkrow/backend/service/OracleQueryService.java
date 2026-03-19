package com.netkrow.backend.service;

import com.netkrow.backend.dto.QueryRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

/**
 * Encapsula toda la lógica de consulta a Oracle (Sterling OMS).
 * El controlador solo delega — nada de JDBC en la capa web.
 */
@Service
public class OracleQueryService {

    private static final int MAX_ROWS = 50;
    private static final int QUERY_TIMEOUT_SECONDS = 120;
    private static final int MAX_ROUTES = 60;
    private static final int MAX_DEPTH = 15;

    @Value("${oracle.datasource.url}")
    private String oracleUrl;

    @Value("${oracle.datasource.username}")
    private String oracleUser;

    @Value("${oracle.datasource.password}")
    private String oraclePassword;

    // ── SELECT libre (solo SELECT permitido) ─────────────────────────────────

    public List<Map<String, Object>> executeQuery(QueryRequest req) throws SQLException {
        String sql = req.getSql().trim();
        if (!sql.toLowerCase().startsWith("select")) {
            throw new IllegalArgumentException("Solo se permiten queries SELECT");
        }

        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(oracleUrl, oracleUser, oraclePassword);
             Statement stmt = conn.createStatement()) {

            stmt.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
            stmt.setMaxRows(MAX_ROWS);

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
            throw new RuntimeException("Error ejecutando query Oracle: " + e.getMessage(), e);
        }
        return result;
    }

    // ── Backtrace (journey backwards) ────────────────────────────────────────

    public Map<String, Object> backtrace(String search, int maxDepth) throws SQLException {
        List<List<Map<String, Object>>> allRoutes = new ArrayList<>();
        List<Map<String, Object>> allTxDetails = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(oracleUrl, oracleUser, oraclePassword)) {
            String sql = "SELECT SUB_FLOW_KEY, SERVER_KEY, FLOW_KEY FROM uatconf.yfs_sub_flow " +
                         "WHERE DBMS_LOB.INSTR(config_xml, ?) > 0 ORDER BY modifyts DESC";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, search);
                try (ResultSet rs = ps.executeQuery()) {
                    Set<String> visited = new HashSet<>();
                    boolean foundAny = false;
                    while (rs.next()) {
                        foundAny = true;
                        if (allRoutes.size() >= MAX_ROUTES) break;
                        findAllParentRoutesFull(
                                search,
                                rs.getString("FLOW_KEY"),
                                rs.getString("SERVER_KEY"),
                                rs.getString("SUB_FLOW_KEY"),
                                new LinkedList<>(),
                                allRoutes,
                                conn,
                                visited,
                                0,
                                maxDepth,
                                allTxDetails
                        );
                    }
                    if (!foundAny) {
                        List<Map<String, Object>> txNodes = findTransactionNodeDetailed(search, conn);
                        for (Map<String, Object> tx : txNodes) {
                            tx.put("type", "transaction");
                            tx.put("label", tx.get("TRANSACTION_KEY"));
                            List<Map<String, Object>> txRoute = new ArrayList<>();
                            txRoute.add(tx);
                            Map<String, Object> finalNode = new LinkedHashMap<>();
                            finalNode.put("type", "service");
                            finalNode.put("label", search);
                            txRoute.add(finalNode);
                            allRoutes.add(txRoute);
                        }
                        allTxDetails.addAll(txNodes);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error en backtrace Oracle: " + e.getMessage(), e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("routes", allRoutes);
        result.put("transactions", allTxDetails);
        return result;
    }

    // ── Métodos privados de traversal ─────────────────────────────────────────

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
            List<Map<String, Object>> allTxDetails
    ) throws SQLException {
        if (depth > maxDepth || allRoutes.size() >= MAX_ROUTES) return;

        String nodeVisitKey = (flowKey + ":" + subFlowKey).trim();
        if (visited.contains(nodeVisitKey)) return;
        visited.add(nodeVisitKey);

        Map<String, Object> flowMeta = findFlowMeta(flowKey, conn);
        String flowName = flowMeta != null ? (String) flowMeta.get("FLOW_NAME") : null;

        if (depth == 0) {
            Map<String, Object> step = new LinkedHashMap<>();
            step.put("type", "service");
            step.put("label", flowToFind);
            step.put("subFlowKey", subFlowKey);
            step.put("flowKey", flowKey);
            step.put("serverKey", serverKey);
            step.put("flowName", flowName);
            step.putAll(flowMeta);

            if (flowName != null && !flowName.trim().isEmpty() && !flowName.equals(flowToFind)) {
                Map<String, Object> flowNode = new LinkedHashMap<>();
                flowNode.put("type", "flow");
                flowNode.put("label", flowName);
                flowNode.put("subFlowKey", subFlowKey);
                flowNode.put("flowKey", flowKey);
                flowNode.put("serverKey", serverKey);
                flowNode.put("flowName", flowName);
                flowNode.putAll(flowMeta);
                currentPath.addFirst(step);
                currentPath.addFirst(flowNode);
            } else {
                currentPath.addFirst(step);
            }
        } else {
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

        boolean foundParent = false;
        if (flowName != null && !flowName.trim().isEmpty()) {
            String sql = "SELECT SUB_FLOW_KEY, SERVER_KEY, FLOW_KEY FROM uatconf.yfs_sub_flow " +
                         "WHERE DBMS_LOB.INSTR(config_xml, ?) > 0 ORDER BY modifyts DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, flowName.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        foundParent = true;
                        findAllParentRoutesFull(
                                flowName.trim(),
                                rs.getString("FLOW_KEY"),
                                rs.getString("SERVER_KEY"),
                                rs.getString("SUB_FLOW_KEY"),
                                new LinkedList<>(currentPath),
                                allRoutes,
                                conn,
                                new HashSet<>(visited),
                                depth + 1,
                                maxDepth,
                                allTxDetails
                        );
                    }
                }
            }
        }

        if (!foundParent) {
            List<Map<String, Object>> txNodes = findTransactionNodeDetailed(
                    flowName != null ? flowName : flowToFind, conn);
            if (!txNodes.isEmpty()) {
                for (Map<String, Object> tx : txNodes) {
                    tx.put("type", "transaction");
                    tx.put("label", tx.get("TRANSACTION_KEY"));
                    List<Map<String, Object>> fullRoute = new ArrayList<>();
                    fullRoute.add(tx);
                    fullRoute.addAll(currentPath);
                    allRoutes.add(fullRoute);
                    allTxDetails.add(tx);
                    if (allRoutes.size() >= MAX_ROUTES) return;
                }
            } else {
                allRoutes.add(new ArrayList<>(currentPath));
            }
        }
    }

    private Map<String, Object> findFlowMeta(String flowKey, Connection conn) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        if (flowKey == null || flowKey.trim().isEmpty()) return result;
        String q = "SELECT FLOW_NAME, OWNER_KEY, PROCESS_TYPE_KEY, FLOW_GROUP_NAME, " +
                   "CREATEUSERID, CREATETS, MODIFYTS FROM uatconf.yfs_flow WHERE TRIM(flow_key) = TRIM(?)";
        try (PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setString(1, flowKey.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result.put("FLOW_NAME", rs.getString("FLOW_NAME"));
                    result.put("OWNER_KEY", rs.getString("OWNER_KEY"));
                    result.put("PROCESS_TYPE_KEY", rs.getString("PROCESS_TYPE_KEY"));
                    result.put("FLOW_GROUP_NAME", rs.getString("FLOW_GROUP_NAME"));
                    result.put("CREATEUSERID", rs.getString("CREATEUSERID"));
                    result.put("CREATETS", rs.getString("CREATETS"));
                    result.put("MODIFYTS", rs.getString("MODIFYTS"));
                }
            }
        }
        return result;
    }

    private List<Map<String, Object>> findTransactionNodeDetailed(String flowName, Connection conn) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        if (flowName == null || flowName.trim().isEmpty()) return result;
        String q = "SELECT f.flow_name as FLOW_NAME, a.action_key as ACTION_KEY, a.actionname as ACTIONNAME, " +
                "a.group_id as GROUP_ID, a.createuserid as CREATEUSERID, e.transaction_key as TRANSACTION_KEY, " +
                "e.eventid as EVENTID, t.process_type_key as PROCESS_TYPE_KEY, t.owner_key as OWNER_KEY, " +
                "t.listener_type as LISTENER_TYPE " +
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
                    result.add(txNode);
                }
            }
        }
        return result;
    }
}

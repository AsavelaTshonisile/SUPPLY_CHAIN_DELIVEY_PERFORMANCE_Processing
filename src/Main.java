import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Main {
    private static final Charset CSV_CHARSET = Charset.forName("windows-1252");

    public static void main(String[] args) {
        Path filePath = Path.of("data", "DataCoSupplyChainDataset.csv");
        Path outputPath = Path.of("output", "processed_orders.csv");

        try {
            List<OrderRecord> orders = loadOrders(filePath);
            Queue<OrderRecord> orderQueue = new LinkedList(orders);
            System.out.println("\nQueue Processing");
            System.out.println("----------------");
            System.out.println("Orders added to queue: " + orderQueue.size());
            OrderRecord nextOrder = (OrderRecord)orderQueue.poll();
            if (nextOrder != null) {
                System.out.println("First order processed: " + nextOrder.getOrderId());
                System.out.println("Shipping difference: " + nextOrder.getShippingDifference());
            }

            System.out.println("Orders remaining in queue: " + orderQueue.size());
            exportOrders(orders, outputPath);
        } catch (IOException e) {
            System.err.println("Could not process the dataset.");
            System.err.println("Reason: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid data encountered.");
            System.err.println("Reason: " + e.getMessage());
        }

    }

    private static List<OrderRecord> loadOrders(Path filePath) throws IOException {
        List<OrderRecord> orders = new ArrayList();
        int validRecords = 0;
        int invalidRecords = 0;

        try (BufferedReader reader = Files.newBufferedReader(filePath, CSV_CHARSET)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("The CSV file is empty.");
            }

            headerLine = headerLine.replace("\ufeff", "");
            List<String> headers = parseCsvLine(headerLine);
            Map<String, Integer> columnIndexes = new HashMap();

            for(int i = 0; i < headers.size(); ++i) {
                columnIndexes.put(((String)headers.get(i)).trim(), i);
            }

            String[] requiredColumns = new String[]{"Order Id", "Days for shipping (real)", "Days for shipment (scheduled)", "Order Profit Per Order", "Delivery Status", "Shipping Mode", "Market", "Order Region"};

            for(String column : requiredColumns) {
                if (!columnIndexes.containsKey(column)) {
                    throw new IOException("Required column is missing: " + column);
                }
            }

            int lineNumber = 1;

            String line;
            while((line = reader.readLine()) != null) {
                ++lineNumber;
                if (!line.isBlank()) {
                    try {
                        List<String> values = parseCsvLine(line);
                        String orderId = getValue(values, columnIndexes, "Order Id");
                        int actualDays = Integer.parseInt(getValue(values, columnIndexes, "Days for shipping (real)"));
                        int scheduledDays = Integer.parseInt(getValue(values, columnIndexes, "Days for shipment (scheduled)"));
                        double orderProfitPerOrder = Double.parseDouble(getValue(values, columnIndexes, "Order Profit Per Order"));
                        String deliveryStatus = getValue(values, columnIndexes, "Delivery Status");
                        String shippingMode = getValue(values, columnIndexes, "Shipping Mode");
                        String market = getValue(values, columnIndexes, "Market");
                        String orderRegion = getValue(values, columnIndexes, "Order Region");
                        if (!orderId.isBlank() && actualDays >= 0 && scheduledDays >= 0 && Double.isFinite(orderProfitPerOrder) && !deliveryStatus.isBlank() && !shippingMode.isBlank() && !market.isBlank() && !orderRegion.isBlank()) {
                            OrderRecord order = new OrderRecord(orderId, actualDays, scheduledDays, orderProfitPerOrder, deliveryStatus, shippingMode, market, orderRegion);
                            orders.add(order);
                            ++validRecords;
                        } else {
                            ++invalidRecords;
                            System.out.println("Invalid data at line " + lineNumber);
                        }
                    } catch (IllegalArgumentException e) {
                        ++invalidRecords;
                        System.out.println("Skipping invalid row at line " + lineNumber + ": " + e.getMessage());
                    }
                }
            }
        }

        System.out.println("\nValidation Summary");
        System.out.println("------------------");
        System.out.println("Valid records: " + validRecords);
        System.out.println("Invalid records: " + invalidRecords);
        System.out.println("Total processed: " + (validRecords + invalidRecords));
        return orders;
    }

    private static void exportOrders(List<OrderRecord> orders, Path outputPath) throws IOException {
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            writer.write("Order ID,Actual Shipping Days,Scheduled Shipping Days,Shipping Difference,Order Profit Per Order,Delivery Status,Shipping Mode,Market,Order Region");
            writer.newLine();

            for(OrderRecord order : orders) {
                String var10001 = csvEscape(order.getOrderId());
                writer.write(var10001 + "," + order.getActualShippingDays() + "," + order.getScheduledShippingDays() + "," + order.getShippingDifference() + "," + order.getOrderProfitPerOrder() + "," + csvEscape(order.getDeliveryStatus()) + "," + csvEscape(order.getShippingMode()) + "," + csvEscape(order.getMarket()) + "," + csvEscape(order.getOrderRegion()));
                writer.newLine();
            }
        }

        System.out.println("\nExport completed successfully.");
        System.out.println("Exported records: " + orders.size());
        System.out.println("Output file: " + String.valueOf(outputPath.toAbsolutePath()));
    }

    private static String csvEscape(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static String getValue(List<String> values, Map<String, Integer> columnIndexes, String columnName) {
        Integer index = (Integer)columnIndexes.get(columnName);
        if (index == null) {
            throw new IllegalArgumentException("Column not found: " + columnName);
        } else if (index >= values.size()) {
            throw new IllegalArgumentException("Missing value for column: " + columnName);
        } else {
            return ((String)values.get(index)).trim();
        }
    }

    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList();
        StringBuilder value = new StringBuilder();
        boolean insideQuotes = false;

        for(int i = 0; i < line.length(); ++i) {
            char character = line.charAt(i);
            if (character == '"') {
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    value.append('"');
                    ++i;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (character == ',' && !insideQuotes) {
                values.add(value.toString());
                value.setLength(0);
            } else {
                value.append(character);
            }
        }

        values.add(value.toString());
        return values;
    }
}

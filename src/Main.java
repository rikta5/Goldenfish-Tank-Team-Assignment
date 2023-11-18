import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws IOException {
        String filePath = "src/Google Play Store Apps.csv";

        Map<String, Integer> categoryApps = new HashMap<>();
        getCategoryApps(filePath, categoryApps);
        createFileNumberOfAppsPerCategory(categoryApps);

        Map<String, Integer> companyApps = new HashMap<>();
        getCompanyApps(filePath, companyApps);
        createFileTop100Companies(companyApps);

        Map<String, Developer> developers = new HashMap<>();
        getDevelopers(filePath, developers);
        createFileTopDevelopers(developers);

        calculateAppsToBuy(filePath, 1000, "NumberOfAppsFor1000Dollars.csv");
        calculateAppsToBuy(filePath, 10000, "NumberOfAppsFor10000Dollars.csv");

        Map<String, Long> downloadCounts = new HashMap<>();
        calculateDownloads(filePath, downloadCounts);
        writeDownloadsToFile(downloadCounts);
    }

    private static void getCategoryApps(String filepath, Map<String, Integer> categoryApplications) throws FileNotFoundException {
        File f = new File(filepath);
        Scanner scanner = new Scanner(f);

        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            String[] values = customSplit(line);

            String category = values[2].trim();
            categoryApplications.put(category, categoryApplications.getOrDefault(category, 0) + 1);

        }

        scanner.close();
    }

    private static String[] customSplit(String line) {

        Pattern pattern = Pattern.compile("(?<=,|^)(\"(?:[^\"]|\"\")*\"|[^,]*)(?:,|$)");
        Matcher matcher = pattern.matcher(line);

        List<String> values = new ArrayList<>();

        while (matcher.find()) {

            String value = matcher.group(1).replaceAll("\"\"", "\"").trim();

            // Handle consecutive commas
            if (value.isEmpty() && matcher.group(0).equals(",")) {
                values.add("");  // Add an empty field
            } else {
                values.add(value);
            }
        }

        return values.toArray(new String[0]);
    }

    public static void createFileNumberOfAppsPerCategory(Map<String, Integer> categoryCountApps) {
        FileWriter output;
        try {
            output = new FileWriter("AppsPerCategory.csv");
            output.write("Category" + "," + "NumberOfApps" + "\n");

            for (Map.Entry<String, Integer> entry : categoryCountApps.entrySet()) {
                output.append(entry.getKey()).append(",").append(String.valueOf(entry.getValue())).append("\n");
            }

            output.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void getCompanyApps(String filepath, Map<String, Integer> companyApplications) throws FileNotFoundException {
        File f = new File(filepath);
        Scanner scanner = new Scanner(f);

        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            String[] values = customSplit(line);

            String appId = values[1].trim();
            String company = extractCompanyName(appId);

            companyApplications.put(company, companyApplications.getOrDefault(company, 0) + 1);

        }

        scanner.close();
    }

    private static String extractCompanyName(String appId) {

        String[] parts = appId.split("\\.");
        if (parts.length >= 2) {
            return parts[0] + "." + parts[1];
        } else {
            return appId;
        }
    }

    private static void createFileTop100Companies(Map<String, Integer> companyCountApps) {
        FileWriter output;
        try {
            output = new FileWriter("Top100Companies.csv");
            output.write("Company" + "," + "NumberOfApps" + "\n");

            List<Map.Entry<String, Integer>> sortedEntries = companyCountApps.entrySet().stream()
                    .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue())).toList();

            sortedEntries = sortedEntries.subList(0, Math.min(sortedEntries.size(), 100));
            //creates a sublist of an already existing list starting from index 0
            //and the size is limited to 100 or less depending on size of the list (if there are fewer than 100 entries)

            for (Map.Entry<String, Integer> entry : sortedEntries) {
                output.append(entry.getKey()).append(",").append(String.valueOf(entry.getValue())).append("\n");
            }

            output.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void getDevelopers(String filepath, Map<String, Developer> developers) throws FileNotFoundException {
        File f = new File(filepath);
        Scanner scanner = new Scanner(f);

        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] values = customSplitDevelopers(line);

            String appId = values[1].trim();
            String developerEmail = values[14].trim();
            String developerID = values[13].trim();

            if (!developerEmail.isEmpty()) {
                String[] emailParts = developerEmail.split("@");
                if (emailParts.length == 2) {
                    String emailDomain = emailParts[1].toLowerCase();
                    String companyName = extractCompanyName(appId).toLowerCase();

                    boolean isEmployee = emailDomain.contains(companyName);

                    Developer developer = developers.get(developerEmail);

                    if (developer == null) { // meaning that there is no existing key
                        developer = new Developer(developerEmail, developerID, isEmployee);
                        developers.put(developerEmail, developer);
                    }

                    developer.incrementAppCount();
                }
            }
        }

        scanner.close();
    }

    private static String[] customSplitDevelopers(String line) {

        Pattern pattern = Pattern.compile("\"([^\"]*)\"|([^,]+)");
        Matcher matcher = pattern.matcher(line);

        List<String> values = new ArrayList<>();

        while (matcher.find()) {
            values.add(matcher.group().trim());
        }

        return values.toArray(new String[0]);
    }

    private static void createFileTopDevelopers(Map<String, Developer> developers) {
        FileWriter output;
        try {
            output = new FileWriter("Top3DevelopersThatDontWorkForCompany.csv");
            output.write("DeveloperID" + "," + "DeveloperEmail" + "," + "NumberOfApps" + "," + "EmployeeOfTheCompany" + "\n");

            List<Developer> sortedDevelopers = developers.values().stream().sorted().toList();

            for (int i = 0; i < Math.min(sortedDevelopers.size(), 3); i++) {
                Developer developer = sortedDevelopers.get(i);
                output.append(developer.getDeveloperID()).append(",").append(developer.getEmail()).append(",")
                        .append(String.valueOf(developer.getAppCount())).append(",")
                        .append(String.valueOf(developer.isEmployee())).append("\n");
            }

            output.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void calculateAppsToBuy(String filepath, double budget, String outputFileName) throws IOException {
        File f = new File(filepath);
        Scanner scanner = new Scanner(f);

        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }

        List<App> apps = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] values = customSplit(line);
            String priceString = values[9].trim().replace("$", "")
                                                 .replace("M", "")
                                                 .replace(",", "");

            try {
                Double price = Double.parseDouble(priceString);
                apps.add(new App(price));
            } catch (NumberFormatException e) {
                continue;
            }

        }

        scanner.close();

        List<App> sortedapps = apps.stream().sorted().toList();

        int appsCount = 0;
        double remainingBudget = budget;

        for (App app : sortedapps) {
            if (remainingBudget >= app.getPrice()) {
                remainingBudget -= app.getPrice();
                appsCount++;
            }
        }

        FileWriter output;
        try {
            output = new FileWriter(outputFileName);
            output.write("Budget" + "," + "NumberOfApps" + "\n");
            output.append(String.valueOf(budget)).append(",").append(String.valueOf(appsCount)).append("\n");
            output.close();
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void calculateDownloads(String filepath, Map<String, Long> downloadCounts) throws FileNotFoundException {
        File f = new File(filepath);
        Scanner scanner = new Scanner(f);

        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] values = customSplit(line);

            String installs = values[5].trim();

            if (installs.startsWith("\"") && installs.endsWith("\"")) { //za slucaj da je vrijednost izmedju navodnika
                installs = installs.substring(1, installs.length() - 1);
            }
            if(installs.contains(",")) installs = installs.replace(",", "");
            if(installs.contains(".")) installs = installs.replace(".", "");
            if(installs.endsWith("+")) installs = installs.replace("+", "");

            try {

            boolean isFree = Boolean.parseBoolean(values[8].strip());

                if (isFree) {
                    downloadCounts.put("Free Downloads", downloadCounts.getOrDefault("Free Downloads", 0L) + Long.parseLong(installs));
                }   else {
                    downloadCounts.put("Paid Downloads", downloadCounts.getOrDefault("Paid Downloads", 0L) + Long.parseLong(installs));
                }
            }catch(NumberFormatException e){
                continue;
            }
        }

        scanner.close();
    }

    private static void writeDownloadsToFile(Map<String, Long> downloadCounts) {
        FileWriter output;
        try {
            output = new FileWriter("FreeAppsVSPaidAppsInDownloads.csv");
            output.write("Category" + "," + "NumberOfDownloads" + "\n");

            for (Map.Entry<String, Long> entry : downloadCounts.entrySet()) {
                output.append(entry.getKey()).append(",").append(String.valueOf(entry.getValue())).append("\n");
            }
            output.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
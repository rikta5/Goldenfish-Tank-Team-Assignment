public class Developer implements Comparable<Developer>{
    private final String email;
    private String developerID;
    private Boolean isEmployee;
    private Integer appCount;

    public Developer(String email, String developerID, boolean isEmployee) {
        this.email = email;
        this.developerID = developerID;
        this.appCount = 1;
        this.isEmployee = isEmployee;
    }

    public String getEmail() {
        return email;
    }

    public Integer getAppCount() {
        return appCount;
    }

    public void incrementAppCount() {
        appCount++;
    }

    public Boolean isEmployee() {
        return isEmployee;
    }

    public String getDeveloperID() {return developerID;}

    @Override
    public int compareTo(Developer o) {
        return o.appCount.compareTo(this.appCount);
    }
}

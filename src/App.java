public class App implements Comparable<App> {
    private final Double price;

    public App(Double price) {
        this.price = price;
    }
    public Double getPrice() {
        return price;
    }

    @Override
    public int compareTo(App o) {
        return Double.compare(o.price, this.price);
    }
}

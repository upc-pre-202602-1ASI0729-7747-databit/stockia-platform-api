package com.databit.stockia.platform.restaurantregistration.domain.model.aggregates;

/**
 * The restaurant (tenant). Every other bounded context scopes its data by restaurantId. StockIA serves
 * one restaurant per account.
 */
public class Restaurant {
    /**
     * Coordinates used for the weather of the demand forecast (Lima by default).
     */
    public static final double DEFAULT_LATITUDE = -12.0464;
    public static final double DEFAULT_LONGITUDE = -77.0428;

    private Long id;
    private String name;
    private String address = "";
    private String phone = "";
    private double latitude = DEFAULT_LATITUDE;
    private double longitude = DEFAULT_LONGITUDE;
    /**
     * Owner account. Not a database foreign key: users.restaurant_id already points here.
     */
    private Long ownerUserId;

    private Restaurant(Long id, String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("restaurant.error.name.required");
        this.id = id;
        this.name = name.trim();
    }

    public static Restaurant create(String name) {
        return new Restaurant(null, name);
    }

    public static Restaurant rehydrate(Long id, String name, String address, String phone, double latitude, double longitude, Long ownerUserId) {
        var restaurant = new Restaurant(id, name);
        restaurant.address = address == null ? "" : address;
        restaurant.phone = phone == null ? "" : phone;
        restaurant.latitude = latitude;
        restaurant.longitude = longitude;
        restaurant.ownerUserId = ownerUserId;
        return restaurant;
    }

    public Restaurant updateProfile(String name, String address, String phone) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("restaurant.error.name.required");
        this.name = name.trim();
        this.address = address == null ? "" : address.trim();
        this.phone = phone == null ? "" : phone.trim();
        return this;
    }

    public Restaurant updateLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        return this;
    }

    public Restaurant linkOwner(Long userId) {
        this.ownerUserId = userId;
        return this;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Long getOwnerUserId() { return ownerUserId; }
}

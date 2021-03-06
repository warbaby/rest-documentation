package warbaby.test;

/**
 * <p>User: Zhang Kaitao
 * <p>Date: 13-12-22
 * <p>Version: 1.0
 */
public class User {
    /** 你好111 */ 
    private Long id;
    private String name;
    private int age;
    /** 看不见我看不见我 */
    private short invisible;
    private boolean male;
    private Address address;
    private AddressLombok addressLombok;

    public Long getId() {
        return id;
    }

    /** 你好333 */
    public void setId(Long id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(boolean male) {
        this.male = male;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public AddressLombok getAddressLombok() {
        return addressLombok;
    }

    public void setAddressLombok(AddressLombok addressLombok) {
        this.addressLombok = addressLombok;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != null ? !id.equals(user.id) : user.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}

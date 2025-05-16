package backend.server.domainlevel;

public class Place{
    
    private String name;
    private Address address;
    private String description;
    private boolean atLeastOneActivityRelated;

    public Place(String name, Address address, String description){
        this.name = name;
        this.address = address;
        this.description = description;
        this.atLeastOneActivityRelated = false;
    }

    public String getName(){
        return name;
    }

    public Address getAddress(){
        return address;
    }

    public String getDescription(){
        return description;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setAddress(Address address){
        this.address = address;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public boolean getAtLeastOneActivityRelated(){
        return atLeastOneActivityRelated;
    }

    public void setAtLeastOneActivityRelated(boolean atLeastOneActivityRelated){
        this.atLeastOneActivityRelated = atLeastOneActivityRelated;
    }

    @Override
    public String toString(){
        //TODO ADDRESS TO STRING
        return "------------"+"\nPLACE " + "\nName: " + name +"\nAddress:" + address.toString() + "\nDescription: " + description+  "\n------------";
    }
}
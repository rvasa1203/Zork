import java.util.ArrayList;

public class Inventory {
    //Collection of Item objects
    private ArrayList<Item> items; 

    public Inventory() {
        items = new ArrayList<Item>();
    }

    public boolean addItem(Item item){
        return items.add(item);
    }


    public Item removeItem(String name) { // returns item based on name given, if item is not in the inventory, it returns null
        for(int i=0; i<items.size(); i++){
            if(name.equals(items.get(i).getName())) {
                return items.remove(i);
            }
        }

        return null; 
    }

    public String toString() {
        String msg = "";

        for(Item i : items){
            msg += i.getName() + "\n";
        }

        return msg;
    }

}

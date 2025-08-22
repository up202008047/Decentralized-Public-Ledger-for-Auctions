package ssd.proj.p2p;

import java.util.LinkedList;
import java.util.List;

public class Bucket {
    private List<Contact> contacts;
    private int bucketCapacity;

    public Bucket(int bucketCapacity) {
        this.contacts = new LinkedList<>();
        this.bucketCapacity = bucketCapacity;
    }

    public void addOrUpdateContact(Contact contact) {
        if (contacts.contains(contact)) {
            contacts.remove(contact);
            contacts.add(0, contact);
        } else {
            if (contacts.size() < bucketCapacity) {
                contacts.add(contact);
            } else {
                contacts.remove(contacts.size() - 1);
                contacts.add(contact);
            }
        }
    }

    public void removeContact(Contact contact) {
        contacts.remove(contact);
    }

    public List<Contact> getContacts() {
        return new LinkedList<>(contacts);
    }

}

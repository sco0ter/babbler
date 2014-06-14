# Writing custom Extensions
---

Writing your own extensions is pretty easy and robust. This is where this library really shines. You basically only have to write a class and annotate it with JAXB annotations.

Let's consider the following example, where you want to send a message with some structured data about a product.

First you have to write the Product class with JAXB annotations:

```
@XmlRootElement(name = "product", namespace = "com:mycompany:product")
@XmlAccessorType(XmlAccessType.FIELD)
public class Product {

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String price;

    @XmlElement
    private String name;

    @XmlElement
    private String description;

    private Product() {
       // Private no-args default constructor for JAXB.
    }

    public Product(String id, String price, String name, String description) {
        this.id = id;
        this.price = price;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
```

Then you have to register that class **before** creating the session. (That is because the session creates the JAXB context in its constructor, it can\'t be modified later):

```java
XmppContext.getDefault().registerExtension(Product.class);
```

*(This is the current approach for registering custom extensions. The idea is that you can later have different contexts you can choose from, e.g. a minimal \'core\' context, an \'advanced\' context or a \'jingle\' context. I am still not sure, if this has any benefit (e.g. for performance). Comments appreciated.)*

You can then simply send a message with that extension:

```java
Message message = new Message(Jid.valueOf("romeo@example.net"));
message.getExtensions().add(new Product("1", "5.99 €", "New product", "A very cool product!!"));
xmppSession.send(message);
```

Which will result in the following stanza on the XMPP stream:

```xml
<message to="romeo@example.net">
    <product xmlns="com:mycompany:product" id="1" price="5.99 €">
        <name>New product</name>
        <description>A very cool product!!</description>
    </product>
</message>
```

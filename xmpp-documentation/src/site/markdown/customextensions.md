# Writing Custom Extensions
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

Note: Experience has shown that it's often easier and more intuitive to declare the namespace once for the whole package. Otherwise it can happen that JAXB assumes another namespace for certain elements (e.g. subclasses, lists, more complex types) and fails to (un)marshal as expected (unless you redeclare the namespace on the elements).

Create a `package-info.java` and put JAXB annotations in it, like this:

```
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSchema(namespace = "yournamespace", elementFormDefault = XmlNsForm.QUALIFIED)
package yourpackage;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
```


Then you have to create a configuration for the session. If you only use core functionality, you have to extend the `CoreContext` class by your extension:

```java
XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
    .context(new CoreContext(Product.class))
    .build();
```

If you want to use all XEP extensions as well as your extension, you have to extend the `ExtensionContext`:

```java
XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
    .context(new ExtensionContext(Product.class))
    .build();
```


This will create the `JAXBContext` with your class (in addition to all other XMPP classes).

Then create the session with that configuration:

```java
XmppSession xmppSession = new XmppSession("domain", configuration);
```

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

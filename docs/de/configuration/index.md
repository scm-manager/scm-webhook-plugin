---
title: Konfiguration
---
Es gibt eine globale und eine repository-spezifische Konfiguration für das Webhook-Plugin. Globale Webhooks werden für
alle Repositories der SCM-Manager-Instanz ausgeführt.

Es ist möglich, durch andere SCM-Manager-Plugins weitere Arten von Webhooks zu erstellen. Diese werden ggf. in diesen
Plugins beschrieben. Das Webhook-Plugin an sich stellt nur einen einfachen Webhook zur Verfügung:

Beim Registrieren eines solchen Webhooks kann man zwischen den Request-Typen "GET", "POST", "PUT" und "AUTO" wählen.
Wird "AUTO" gewählt, sendet der Webhook einen GET-Request an die angegebene URL,
es sei denn, es werden die Commit Daten mit dem Request übermittelt. In diesem Fall sendet der Webhook einen POST-Request.
In das Feld für die URL muss der genaue Endpunkt eingetragen werden, an die der Webhook den Request senden soll.

Im Standardfall wird ein Webhook pro Repository Push getriggert. Über eine Checkbox kann der Webhook aber auch pro Commit
ausgelöst werden. Die zweite Checkbox in der Konfiguration sorgt dafür, dass der Webhook die Commit Daten an den Request anhängt.

![Webhook Konfiguration](assets/config.png)

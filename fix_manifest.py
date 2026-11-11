import re

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

service_tag = """        <service
            android:name=".NetworkMonitorService"
            android:exported="false"
            android:foregroundServiceType="dataSync" />"""

if service_tag in content:
    content = content.replace(service_tag, "")

with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(content)

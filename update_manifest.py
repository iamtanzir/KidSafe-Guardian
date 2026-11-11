import re

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

# Add FOREGROUND_SERVICE_DATA_SYNC permission
if 'android.permission.FOREGROUND_SERVICE_DATA_SYNC' not in content:
    content = content.replace(
        '<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />',
        '<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />\n    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />'
    )

service_tag = """        <service
            android:name=".NetworkMonitorService"
            android:exported="false"
            android:foregroundServiceType="dataSync" />"""

if 'NetworkMonitorService' not in content:
    content = content.replace('</application>', f'{service_tag}\n    </application>')

with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(content)

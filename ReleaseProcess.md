# Release process for pmix #

## First time ##
We have to use a self-signed certificate to sigining the apk files... I generated this key, if you need this ask me (stefan.agner)
```
keytool -genkey -v -keystore pmix.keystore -alias pmix -keyalg RSA -validity 10000
```
I used this values for the certificate:
```
CN=PMix developer, OU=Unknown, O=pmix, L=Unknown, ST=Unknown, C=org
```

To change the keypassword, use this command:
```
keytool -keypasswd -v -keystore pmix.keystore -alias pmix
```

To export the Key (e.g. to transfer to another developer) use this command:
```
keytool -exportcert -v -keystore pmix.keystore -alias pmix ...
```

## Every release ##
  1. Test the application! :-)
  1. Check android:versionName and android:versionCode in AndroidManifest.xml
  1. Remove android:debuggable="true" from the Element 

&lt;application&gt;

 AndroidManifest.xml (if exists)
  1. Remove log files, backup files, and other unnecessary files from the application project (not needed normally)
  1. Deactivate any calls to Log methods in the source code (I don't did this at my releases)
  1. Call "Project" "Clean", just to make sure all is recompiled right...
  1. Right Click on the pmix project, choose "Android Tools" and "Export Unsigned Application Package..."
  1. Save the File as PMix.apk
  1. Sign the apk file with jarsigner -verbose -keystore pmix.keystore PMix.apk pmix
  1. Verify the signed apk file with jarsigner -verify -verbose -certs PMix.apk
  1. Align the Zip-File with zipalign 4 pmix-0.4-beta3.apk pmix-0.4-beta3-aligned.apk
  1. svn copy https://pmix.googlecode.com/svn/trunk https://pmix.googlecode.com/svn/branches/0.3 --username stefan.agner
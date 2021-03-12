# Eartag-Reader
Reads the Tvdnumber from the earmarks of sheeps

## Compiling Eartag-Reader
To compile the app after version 1.2.1 (more precisely after commit e15ae57e9df65d66020e68c4021f91385ceded0d) it's necessary to add a file called ```keystore.properties``` in the project folder.
Its needed to sign the compiled apk.
Below is the content of ```keystore.properties```.
```
## This file must *NOT* be checked into Version Control Systems,
# as it contains information specific to your local configuration.

storeFile={location of jks file for this project}
storePassword={password for jks file}
keyAlias={keyAlias for this project}
keyPassword={password for key}
```

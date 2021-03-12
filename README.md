# Eartag-Reader
Reads the Tvdnumber from the earmarks of sheeps

## Compiling Eartag-Reader
To compile the app after version 1.2.1 it's necessary to add a file called ```keystore.properties``` in the project folder.
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

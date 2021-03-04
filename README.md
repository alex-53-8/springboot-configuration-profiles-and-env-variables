# SpringBoot configuration aspects

### Disclaimer
This article does not cover correctness of defining configuration keys in a file, it's just an example how 
to use overriding for customizing configuration. Therefore my examples contain users' names and password in configuration files, 
it's just an example :) Personally I don't store credentials in configuration files and supply credentials via environmental variables.


## How to check projects' configuration
Just start the application and make following HTTP call:
```http request
GET http://localhost:8080/configuration
```

And you get similar response.
```json
{
  "port": 9090,
  "credentials": {
    "username": "${APP_USERNAME}",
    "password": "${APP_PASSWORD}"
  },
  "services": {
    "payment": {
      "url": "http://localhost:9090",
      "frequency": 1800
    }
  },
  "storage": {
    "clearAfterDays": 10
  }
}
```

## Spring profiles and configuration files
Why do you need profiles in SpringBoot.
> Spring Profiles provide a way to segregate parts of your application configuration and make it be available only in certain environments.

By default there is `default` profile that exists all the time. A developer is able to introduce as many profiles' names as needed.
There are few interesting parts for applying profiles to configuration files.

`application.yml` or `application.properties` holds configuration of a `default` profile. If a developers wants to introduce 
a configuration file for a specific profile the following name conversion is followed: `application-PROFILE_NAME.yml` 
where `PROFILE_NAME` is a profile's name. All configuration keys that are stored in such files are associated with 
profile only and will appear in an application once a developer applies a profile's name during application's startup. 

## How configuration keys' map is created
During startup a configuration keys' map is created. The map is filled with the keys and values from configuration files. 
Not all configuration files are read - only those files are read that match active profiles and `default` one.
If the same configuration keys are exist then the value of such key is overridden by the latest specified profile. 
There is a simple order of reading configuration files: `default` (`application.yml`) is read every time first and then
only other configuration files are read in the order a developer specified active profiles.

### How to activate a profile
There are two ways for activating spring profiles:
#### JVM property
Specify a property `spring.profiles.active` and list desired profiles comma-separated
 
 `-Dspring.profiles.active=profile1,profile2,profile3`
 
#### Environmental property
Create an environmental variable `SPRING_PROFILES_ACTIVE` and  and list desired profiles comma-separated

`SPRING_PROFILES_ACTIVE=profile1,profile2,profile3`

### Profiles' order matter?
Lets discover two examples how order of specifying profiles affects resulting configuration's map.

There are two profiles `dev` and `local` in both examples, but activation is written in different way.
Both profiles has the same configuration keys, but with different values.

Dev profile configuration (`application-dev.yml`)
```yml
server:
    port: 8080
```

Local profile configuration (`application-local.yml`)
```yml
server:
    port: 9090
```

Profiles could be specified as following 
1. `java -Dspring.profiles.active=dev,local`;
Due to `local` profile is specified as the latest one then a key `server.port` is overridden by a value `9090` from `local` profile
```yml
server:
    port: 9090
```

2. `java -Dspring.profiles.active=local,dev`
Due to `dev` profile is specified as the latest one then a key `server.port` is overridden by a value `8080` from `dev` profile
```yml
server:
    port: 8080
```

As you seen an order of profiles specification matters to get correct values in the configuration's map.

## Overriding configuration by a configuration profile
The main subject of the section was mostly described in the previous section when creation of a configuration map was described.
In that section I will show some real-life samples of profiles usage.

Consider that in a production environment an application uses `application.yml` as a main configuration file. The file
specifies main url, credentials, and other configuration parameters. Apart from production there is a development 
environment. Both environments in configuration scope are the same, but configuration keys contain different values, like
database URL, database user's name, application's port, routes, and so on. Some configuration properties are constant, but
some configuration properties are dependent on an environment we are currently working with. 

It's not good just copy-paste content of file among all profiles.

It's a good practice just to override certain crucial configuration values in a production configuration to make the
application work in a development environment.

Lets consider following production's configuration. User name and password are provided from environmental variables.

Configuration file - `application.yml`
```yaml
app:
  port: 9090
  services:
    payment:
      url: http://localhost:9090
      frequency: 1800

  credentials:
    username: ${APP_USERNAME}
    password: ${APP_PASSWORD}

  storage:
    clear_after_days: 10
```

Consider that every developer has the same user name and password in their local environments, we can override just part 
of credentials.

Configuration file - `application-local.yml`
```yaml
app:
  credentials:
    username: test
    password: test
```

And when a developer start the application it's required to specify a profile `local` to have following resulting configuration 
that is created in the application.

```json
{
  "port": 9090,
  "credentials": {
    "username": "test",
    "password": "test"
  },
  "services": {
    "payment": {
      "url": "http://localhost:9090",
      "frequency": 1800
    }
  },
  "storage": {
    "clearAfterDays": 10
  }
}
```

The example above shown just small overriding. That approach is very convenient when your configuration file is really huge,
but you nned to have just partial changes that are environment dependent. Also, it's convenient for testing - assigning
mock url and other values, but in overall the configuration file is the same, but has overloaded values specifically for your need.

## Assigning values in configuration from environmental variables
It's quite simple to assign to a configuration's key a value from an environmental variable. A developer just need to
use a name of the variable, wrap it a special construction `${VARIABLE_NAME}`, and assign to a desired key.

How to define environmental variables ([Windows](https://www.computerhope.com/issues/ch000549.htm), [Linux](https://askubuntu.com/questions/58814/how-do-i-add-environment-variables)).

Assuming there are following environmental variables that define a user' name and a password. That values should be applied
in a configuration.
```text
APP_USERNAME=root
APP_PASSWORD=root_password
```

A configuration file `application.yml` contains following block. In that block properties `username` and `password` 
are assigned with values from environmental variables `APP_USERNAME` and `APP_PASSWORD` correspondingly during startup of the application.
```yaml
app:
  ...
  credentials:
    username: ${APP_USERNAME}
    password: ${APP_PASSWORD}
```

Configuration values after startup looks like.
```json
{
  "port": 9090,
  "credentials": {
    "username": "root",
    "password": "root_password"
  },
  "services": {
    "payment": {
      "url": "http://localhost:9090",
      "frequency": 1800
    }
  },
  "storage": {
    "clearAfterDays": 10
  }
}
```

## Override configuration keys' values by environmental variables
It's possible to overriding of a configuration's value that is hardcoded in a configuration file by defining 
an environmental variable. The only one rule is applicable to the variable's name: the variable's name has to equals key's name 
in snake case in a configuration file.

Yaml format
```yaml
app:
  storage:
    clear_after_days: 10
```

a name of an environmental variable has to be `APP_STORAGE_CLEAR_AFTER_DAYS`.
Delimiters like `.` in properties file or `:` in yaml are replaced with an underscores.

Environmental variable, that is defined below, replaces configuration key's value `10` that is defined above in Yaml format with a new value `25`
```text
APP_STORAGE_CLEAR_AFTER_DAYS=25
```

Configuration values after startup looks like, value of `app.storage.clear_after_days` has been changed from 10 to 25.
```json
{
  "port": 9090,
  "credentials": {
    "username": "root",
    "password": "root_password"
  },
  "services": {
    "payment": {
      "url": "http://localhost:9090",
      "frequency": 1800
    }
  },
  "storage": {
    "clearAfterDays": 25
  }
}
```

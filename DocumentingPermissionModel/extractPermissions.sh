#!/bin/sh
if [ -n "$GROOVY_HOME" ]; then
    # change the dir to the root of the client directory
    SHELL_NAME="$0"
    SHELL_PATH=`dirname "${SHELL_NAME}"`
    
    if [ "." = "$SHELL_PATH" ]
    then
       SHELL_PATH=`pwd`
    fi

    groovycmd="$GROOVY_HOME/bin/groovy"
    jarfile="$SHELL_PATH/uDeployRestClient.jar"
    
    if [ -r "$jarfile" ]; then
	"$groovycmd" -cp "$jarfile" extractPermissions.groovy "$@"
    else
        echo "Didn't find $jarfile in directory ${SHELL_PATH}"
        exit 1
    fi
else
    echo You must have GROOVY_HOME set in your environment to use the extractPermissions tool.
    exit 1
fi

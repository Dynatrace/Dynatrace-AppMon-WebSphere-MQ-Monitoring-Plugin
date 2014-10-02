<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>WebSphere MQ Monitoring Plugin</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8" />
    <meta content="Scroll Wiki Publisher" name="generator"/>
    <link type="text/css" rel="stylesheet" href="css/blueprint/liquid.css" media="screen, projection"/>
    <link type="text/css" rel="stylesheet" href="css/blueprint/print.css" media="print"/>
    <link type="text/css" rel="stylesheet" href="css/content-style.css" media="screen, projection, print"/>
    <link type="text/css" rel="stylesheet" href="css/screen.css" media="screen, projection"/>
    <link type="text/css" rel="stylesheet" href="css/print.css" media="print"/>
</head>
<body>
                <h1>WebSphere MQ Monitoring Plugin</h1>
    <div class="section-2"  id="27623522_WebSphereMQMonitoringPlugin-Overview"  >
        <h2>Overview</h2>
    <p>
            <img src="images_community/download/attachments/27623522/icon.png" alt="images_community/download/attachments/27623522/icon.png" class="confluence-embedded-image image-center" />
        The WebSphere MQ Monitoring plugin enables you to monitor your queues and topics and relate these metrics to your applications overall performance.    </p>
    <p>
With this plugin you can monitor a WebSphere MQ QueueManager, its Queues, Topics and Subscriptions.<br/><strong class=" ">This plugin is also included in our</strong> <strong class=" "><a href="https://community/display/DL/WebSphere+MQ+Monitoring+Fastpack">WebSphere MQ Monitoring Fastpack</a></strong><strong class=" ">, augmented by dashboards and a template system profile!</strong>    </p>
    </div>
    <div class="section-2"  id="27623522_WebSphereMQMonitoringPlugin-PluginDetails"  >
        <h2>Plugin Details</h2>
    <div class="tablewrap">
        <table>
<thead class=" "></thead><tfoot class=" "></tfoot><tbody class=" ">    <tr>
            <td rowspan="1" colspan="1">
        <p>
Plug-In Versions    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
<a href="attachments_77660172_1_com.dynatrace.plugins.mq_2.0.0.jar">WebSphere MQ Monitoring Plugin 2.0.0</a>    </p>
            </td>
        </tr>
    <tr>
            <td rowspan="1" colspan="1">
        <p>
Author    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
Alain Helaili (alain.helaili@dynatrace.com)<br/>Asad Ali (asad.ali@dynatrace.com)    </p>
            </td>
        </tr>
    <tr>
            <td rowspan="1" colspan="1">
        <p>
License    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
<a href="attachments_5275722_2_dynaTraceBSD.txt">dynaTrace BSD</a>    </p>
            </td>
        </tr>
    <tr>
            <td rowspan="1" colspan="1">
        <p>
Support    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
<a href="https://community/display/DL/Support+Levels#SupportLevels-Community">Not Supported </a>    </p>
            </td>
        </tr>
    <tr>
            <td rowspan="1" colspan="1">
        <p>
Known Problems    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
    </p>
            </td>
        </tr>
    <tr>
            <td rowspan="1" colspan="1">
        <p>
Release History    </p>
            </td>
                <td rowspan="1" colspan="1">
        <p>
2010-05-05 Initial Release<br/>2012-03-09 v1.0.7 (SSH)  released<br/>2012-04-02 v2.0.0 (combined SSH with non)    </p>
            </td>
        </tr>
</tbody>        </table>
            </div>
    </div>
    <div class="section-2"  id="27623522_WebSphereMQMonitoringPlugin-ProvidedMeasures"  >
        <h2>Provided Measures</h2>
    <div class="section-3"  id="27623522_WebSphereMQMonitoringPlugin-WebSphereMQQueueManager"  >
        <h3>WebSphere MQ Queue Manager</h3>
<ul class=" "><li class=" ">    <p>
Status<br/>provides the status of the Queue Manager as a numeric value<br/>0: ENDED UNEXPECTEDLY<br/>1: ENDED_PREEMPTIVELY<br/>2: ENDED_IMMEDIATELY<br/>3: ENDED_NORMALLY<br/>4: ENDING_PREEMPTIVELY<br/>5: ENDING_IMMEDIATELY<br/>6: QUIESCING<br/>7: STARTING<br/>8: RUNNING    </p>
</li><li class=" ">    <p>
Running<br/>Is 1 only if the QueueManager is in Running state. If it is ended,starting or in the processes of shutting down it will be 0    </p>
</li><li class=" ">    <p>
Stopped<br/>Is 1 only if the QueueManager is in ended. If it is running, starting or in the processes of shutting down it will be 0    </p>
</li></ul>    </div>
    <div class="section-3"  id="27623522_WebSphereMQMonitoringPlugin-WebSphereMQQueue"  >
        <h3>WebSphere MQ Queue</h3>
<ul class=" "><li class=" ">    <p>
Queue depth<br/>The current depth of the queue, in other words how many messages are currently on it    </p>
</li><li class=" ">    <p>
Duration since last read<br/>The duration in seconds since the last message was read (removed) from the queue.<br/>This measure will only be available if MONQ is enabled via: ALTER QMGR MONQ(LOW)    </p>
</li><li class=" ">    <p>
Duration since last insert<br/>The duration in seconds since the last message was put onto the queue<br/>This measure will only be available if MONQ is enabled via: ALTER QMGR MONQ(LOW)    </p>
</li><li class=" ">    <p>
Oldest message age<br/>The age in seconds of the oldest message on the queue    </p>
</li><li class=" ">    <p>
Input handle count<br/>This is the number of applications that are currently connected to the queue to put messages on the queue.    </p>
</li><li class=" ">    <p>
Output handle count<br/>This is the number of applications that are currently connected to the queue to get messages from the queue.    </p>
</li></ul>    </div>
    <div class="section-3"  id="27623522_WebSphereMQMonitoringPlugin-WebSphereMQTopic"  >
        <h3>WebSphere MQ Topic</h3>
<ul class=" "><li class=" ">    <p>
Publisher Count<br/>The number of applications currently publishing to the topic.    </p>
</li><li class=" ">    <p>
Subscriber Count<br/>This is the number of subscribers for this topic string, including durable subscribers who are not currently connected.    </p>
</li></ul>    </div>
    <div class="section-3"  id="27623522_WebSphereMQMonitoringPlugin-WebSphereMQSubscriptions"  >
        <h3>WebSphere MQ Subscriptions</h3>
<ul class=" "><li class=" ">    <p>
Message count<br/>The number of messages that went through    </p>
</li><li class=" ">    <p>
Duration since last restoration<br/>Elapsed time in seconds since the last restoration of the connection.    </p>
</li><li class=" ">    <p>
Duration since last message<br/>Elapsed time in seconds since the last message was retrieved    </p>
</li></ul>    </div>
    </div>
    <div class="section-2"  id="27623522_WebSphereMQMonitoringPlugin-Configuration"  >
        <h2>Configuration</h2>
    <p>
You need to setup a Monitor in your System Profile.<br/>            <img src="images_community/download/attachments/27623522/WebSphere_MQ_Config.PNG" alt="images_community/download/attachments/27623522/WebSphere_MQ_Config.PNG" class="confluence-embedded-image" />
            </p>
    <p>
Configure the authentication method, port, and user credentials to access your WebSphere MQ installation and configure the MQ Bin directory.    </p>
    <p>
Next configure the type of Object that you want to monitor (Queue, Topic, Subscription) and the Queue Managers to use. As a last item configure the object name appropriately. If you select object type Queue than this is the queue name, if you select a topic than this is the topic string of your topic.<br/>As a note the Queue manager name and the queue name are both case sensitive!    </p>
    <p>
<strong class=" ">IMPORTANT</strong> : The user used to run the dynaTrace collector (which the plugin is being executed on) needs to have sufficient privileges from a MQ point of view. If the queue manager status is ok but all numeric values are null, it is likely that the user does not have enough privileges.    </p>
    </div>
    <div class="section-2"  id="27623522_WebSphereMQMonitoringPlugin-ConfigurationofMQ"  >
        <h2>Configuration of MQ</h2>
    <div class="section-3"  id="27623522_WebSphereMQMonitoringPlugin-Language"  >
        <h3>Language</h3>
    <p>
WebSphere MQ reports its status in the users own language. While this is convenient for the user it is a problem for this plugin. If you are using 7.0.1 or higher you should use the <i class=" ">Force English response</i> option to work around this. If your WebSphere MQ installation is older than that please make sure that the user running the collector and MQ has its language set to English.    </p>
    </div>
    <div class="section-3"  id="27623522_WebSphereMQMonitoringPlugin-MQStatistics"  >
        <h3>MQ Statistics</h3>
    <p>
To get the full value of this plugin your need to enable monitoring either globally at the Queue Manager level or locally at the Queue level. To enable monitoring at the Queue Manager level, execute the following MQSC command in your MQ console:    </p>
    <div class="confbox programlisting">
                <div class="content">
        <pre><code>ALTER QMGR MONQ(LOW)</code></pre>
        </div>
    </div>
    <p>
or from a regular Unix/Windows shell :    </p>
    <div class="confbox programlisting">
                <div class="content">
        <pre><code>echo ALTER QMGR MONQ(LOW) | runmqsc</code></pre>
        </div>
    </div>
    <p>
it will enable some additional statistic values that would otherwise not be available.    </p>
    </div>
    </div>
    <div class="section-2"  id="27623522_WebSphereMQMonitoringPlugin-Installation"  >
        <h2>Installation</h2>
    <p>
Import the Plugin into the dynaTrace Server. For details how to do this please refer to the <a href="https://community/display/DOCDT55/Plugin+Management">dynaTrace documentation</a>.<br/>Next import the two Dashboards attached to this page, they will give you a good starting point.    </p>
    </div>
            </div>
        </div>
        <div class="footer">
        </div>
    </div>
</body>
</html>

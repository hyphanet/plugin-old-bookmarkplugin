This Plugin...
 - WILL CREATE A DIRECTORY 'bookmarks' IN YOUR WORKING DIRECTORY ON STARTUP
 - ... that is used for persistence of your bookmarks, settings and everything
 - has a lot of dependencies
 - needs java 1.5 to compile and run
 - is totally uncommented
 - has problems with urldecoding with the current freenet trunk
 - is mostly untested, including the persistence layer
 - has way too many classes
 - is a huge hack meant as a prototype and for getting to know the Freenet API
 
What can you expect? 
 - once you have logged in with a ssk keypair, you can add bookmarks to keys
 - the keys you bookmark must be available in your node's store
 - after login, your bookmarks are automatically published to a 'slot' every x minutes
 - a 'slot' looks like SSK@asdasdasd,sdsdfsdf,dsfsdf/nacktschneck-2
 - you can change the interval on the "Channels" page
 - to request other users bookmarks, you must know the URI of one of their 'slots'
 - enter this URI on the "Channels" page and the plugin will slowly begin to fetch the slots
 
Some parts may be useful for other plugins or toadlets, but need heavy refactoring
to be used outside of its context.

All dependencies except for freenet classes and freenet-ext.jar can be found in 'lib', 
so you should be able to compile this easily with either eclipse or ant.
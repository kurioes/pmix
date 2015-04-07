# Introduction #

We first used Amazon Service to get Album Covers. Amazon changed theire Authentication scheme. You have to use a private key, which you can't include in public distributed applications (It may work in compiled, but even then, you can find the private key in the binaries). Therefor we changed to Last.fm.


# Last.fm #
Last.fm proviedes album covers over thei're API. But we have to include an indication from where our application gets the data. Therefor I (sag) added an icon in the new about screen.

Janni Kovacs developed a Java Last.fm API Library (available [here](http://www.u-mass.de/lastfm). Its licenced under the BSD licence. Therefor it should be compatible with our licence. But there were several problems running the library under Android, therefor I forked the code and commited it to our svn repository.

Interessting: In the code I found hits that code has to be changed to get it running under Android. I followed that hints and found out that [Łukasz Wiśniewski](http://wisniowy.blogspot.com/) developed an Android Last.fm client. Today, this client is merged with the official Last.fm client ([announcment](http://blog.last.fm/2009/01/23/lastfm-on-android), [code](http://github.com/mxcl/lastfm-android)). It seems that they forked the library, but its under GPL nowadays, so no access for us there (or we have to change the licence...). Nevertheless, we don't want to use the full API, and therefor the work of Janni Kovacs should be enouth for our purpose...
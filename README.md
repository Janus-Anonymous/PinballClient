PinballClient
---

Components
---
This Client project contains:
 - Pinball Library
 - Pinball Client demo

HOW TO USE
---
- First, install [PinballFramework](https://github.com/Pinball-Anonymous/PinballFramework) Service on your Android phone.
- Second, import __PinballBox__ and __PinballClientLib__ modules into your project.
- A sample is already prepared for you in `app` module.
- Enjoy your development with __Pinball__.

Basic Usage
---
```
    PinballFrameworkClient pinballFrameworkClient = PinballFrameworkClient.getInstance(this);
    
    String url = "http://XXXXXXXXXXXXXXXXXXX";
    
    pinballFrameworkClient.putRequest(new ByteRequest(url, PinballRequest.ACTIVE, "New Request"), 
        new Response.Listener<byte[]>() {
        
          /**
          * Do something with call back response in onResponse() method.
          * @param response
          */
          @Override
          public void onResponse(byte[] response) {
              // Design what you need to do with the response here.
          }
          
    });
```

Note
---
Due to the constraints of time, some of the API in this Client project is not well-designed, and some are redundant at all.

Currently `putRequest` can handle most types of request, i.e. Json, text and image, etc. Therefore we highly recommend you to use 
this API.


We will gradually fix them step by step:)
 

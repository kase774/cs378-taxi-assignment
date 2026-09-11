# Please add your team members' names here. 

## Team members' names 

1. Student Name: Adrien Chew

   Student UT EID: auc99

2. Student Name: Kason Gu

   Student UT EID: kkg762

 ...

##  Course Name: CS378 - Cloud Computing 

##  Unique Number: 51515
    


# Add your Project REPORT HERE 

Part 1:

5 lines that our application caught as being bad:

03B354861CC944EEBBDFCC6B205B54BA,53EC0370395F1403B28FD9E2597E4A7F,2013-04-30 03:35:11,2013-04-30 09:35:11,0,0.00,-73.975784,40.759262,,,CRD,40.00,0.50,0.50,13.89,5.33,60.22
11303D7084F16FEBE7AB3FBCC2B6B01C,BB9D690E9AD581BAE0331B1E98610658,2013-05-01 11:32:00,2013-05-01 13:32:00,0,0.00,-73.986397,40.740269,,,CRD,7.50,0.00,0.50,1.00,0.00,9.00
2F9578CADE4D64A0600A113FC4F3C39B,C1059E614E05082428C20540004D03CE,2013-12-12 15:13:29,2014-01-03 16:31:27,529,0.00,-73.949615,40.744804,-73.949066,40.744892,CSH,XXXXXX,,,,,,,,,,,,
507F47C5C2DC51740BFFA7FA2895CB0C,361340A453DB445E4620E8E10D408ED9,2013-05-01 14:33:09,2013-05-01 16:33:09,0,0.00,-73.981102,40.725044,,,CRD,52.00,0.00,0.50,10.50,0.00,63.00
FF89BA8DB91F0C24E1AFC43A22FD399E,032EF994A659885B5BC907B287147FF5,2013-05-02 11:52:00,2013-05-02 12:16:00,0,0.00,-73.958588,40.781597,,,CRD,15.00,0.00,0.50,2.50,0.00,18.00

For our data, we expected there to be a value for each property (though 
this was not an exact demand of the project). This could very
easily be turned off, but as it only affected around 1K rows total (out of the big),
we decided that it was not worth solving.

Part 2:

ut-austin-cs378-cloud-computing-cs378-assignment-1-adrienchew246-main$ tail -n 10 SORTED-FILE-RESULT.txt

E3E4AA22FF0F3AFF58067D89F08633B4,4669D6DB6D5B6739B9194E999D907924,2013-06-20 15:33:58,2013-06-20 15:35:05,66,0.00,-73.952843,40.748081,-73.952827,40.748089,CSH,500.00,0.00,0.00,0.00,0.00,500.00
A955B91E4328BD67AF5C7C46FD449164,7232FA85758481F3AD4217818AD438F8,2013-01-20 21:31:36,2013-01-20 21:32:10,33,0.10,-73.783829,40.646339,-73.784836,40.645996,CRD,500.00,0.00,0.00,0.00,0.00,500.00
E3E4AA22FF0F3AFF58067D89F08633B4,4669D6DB6D5B6739B9194E999D907924,2013-06-20 15:52:15,2013-06-20 15:53:03,48,0.00,-73.952766,40.748112,-73.952766,40.748112,CSH,500.00,0.00,0.00,0.00,0.00,500.00
E3E4AA22FF0F3AFF58067D89F08633B4,4669D6DB6D5B6739B9194E999D907924,2013-06-20 16:03:34,2013-06-20 16:04:25,51,0.00,-73.952782,40.748104,-73.952789,40.748100,CSH,500.00,0.00,0.00,0.00,0.00,500.00
C0FD38C1A720AA5C05707BA6D4DFB567,4297F44B13955235245B2497399D7A93,2013-01-05 07:13:08,2013-01-05 07:15:11,123,0.00,-73.934059,40.752232,-73.934059,40.752232,CSH,500.00,0.00,0.00,0.00,8.25,508.25
3357A44D5456675BF531804275FAEFB3,1ABF43D367B90162166E85F570DB0BFC,2013-02-10 15:37:24,2013-02-10 17:12:26,5701,44.70,-73.814049,40.738701,-73.055969,40.816685,CRD,500.00,0.00,0.00,0.00,0.00,500.00
17A3D1C9C47C5B519AECCE5BB75625B4,C81E728D9D4C2F636F067F89CC14862C,2013-04-23 11:44:15,2013-04-23 11:45:23,68,0.00,-73.929741,40.812202,-73.929741,40.812202,CSH,500.00,0.00,0.00,0.00,0.00,500.00
501409567DDD84E51EFF8379251C581A,2F80C317C9A94DCC5EAA743E87FEBAFC,2013-01-01 04:44:49,2013-01-01 04:47:27,158,1.10,-73.927147,40.825916,-73.939903,40.820793,CSH,500.00,0.00,0.00,0.00,0.00,500.00
970427A5D84D29696630BABA4D11C7F0,7C0BD016A8EC0B73305E8AD9D22B3A5A,2013-01-09 07:23:29,2013-01-09 07:24:14,44,0.00,-73.995903,40.743874,-73.995903,40.743874,CRD,500.00,0.00,0.00,0.00,0.00,500.00
C2B48FE1ED15833D27B8C44667DDB878,9167C1802996E3C7D4E1777D46930646,2013-04-23 08:02:42,2013-04-23 08:03:17,36,0.00,-74.534523,40.724545,-74.534523,40.724545,CRD,500.00,0.00,0.00,0.00,0.00,500.00

We completely rewrote the original program, so there isn't a parameter to pass :(
In addition, our intermediate data was stored in compressed memory rather than files.
The access / retrieval process was similar to reading from files, but we calculated that we had 
enough memory to use compressed memory rather than files for faster performance.

Part 3:

Almost everything that we did was parallel. Since IO was the only part of the application that 
was limited, we tried to avoid that as much as possible (hence the use of compressed memory).

Our core reading process was as follows:

```java
Files.lines(new File(BIG_CSV).toPath(), StandardCharsets.UTF_8)
   .parallel()
   .map(StringSerialization::parseLine)
   .filter(Objects::nonNull) // ...

```

This allowed us to get a stream and parallely process the data.

Once we had our data, we converted them into keys, which, when sorted, would retain the order of 
the parent objects they represented. We could easily store all the keys
in memory (and our original idea was to store only the keys in memory while keeping
the other data in a file). However, we figured out that we could also keep the data in memory,
as long as it was not in Java's object form. Instead, we compressed the memory into a binary
buffer with effectively our own serialization strategy. This greatly decreased the amount of memory
needed; rather than the default ~200 chars (and thus, bytes) needed to represent a row,
we used a statically sized 72bytes. This also made it very easy to read from, since we
could just use index * 72 bytes as the offset.

To make this part multithreaded as well, we couldn't have the data in a single data structure;
rather we created our data structures (data pool) and mapped them per thread. 

We found that we could compress all our key data to 64 bits. Therefore, we used the 
following schema:
 - first 24 bits, representing the fare (which we want to sort by) in cents
 - 8 bits, representing the thread which processed the parent object
 - last 32 bits, representing the index of the parent object in the thread's data pool.

Since the most significant 24 bits represented the fare, we could sort the data
by the keys, and it would act as if we sorted by the fare. The rest of the key data
was information on how to retrieve the data. Thus, after we had sorted all the keys,
we could read the other 40 bits per key in the sorted order, retrieve the data,
and get a sorted list. 

However, even if we knew the order of the objects, we could not just use a list or even a
stream for this final processing section. Since the cost of finding data from memory was so much 
lower than the cost of writing it to the output file, we found that, from our tests, that
all the key data would be uncompressed and sit in the pipeline waiting to be written using
Java streams. The raw data, in Java object format, would use too much memory for the 16G of 
memory we allocated to the JVM on our tests. Our initial solution was to delegate 1 thread
to reading the sorted long array and writing it to file; however, this took too long (around
86 seconds for a full run).

In order to speed this up, we created a rotating string buffer system, where all the other threads 
do the processing to map our compressed data to string form, 
and then one thread pulls from the buffer continuously to write to file. This decreased the amount
of work done on the writing thread significantly, almost halving the amount of time needed
for the operation. The full operation could not be completed in 40 seconds (given that it
took around 12 seconds to read from disk, 20 seconds to write from disk, a min of 32 seconds)

We believe this is close to the fastest possible speed possible for this task.
The critique of our implementation is that, while it utilizes parallelism to a much
greater degree than possible with other, file-based implementations, it's much less
scalable as a result, since it can't handle data in the same sense of a file based one,
due to the limit posedd by memory. The implementation structure we used was only chosen
because it fit for the dataset required. We have successfully run the app with only 
10G of memory though, which implies that the current approach can scale to around 25G
csv files. To improve scalability though, we can easily convert the data pools we've been
using in compressed memory to be files, since we already interact with them through
data streams anyways. This would mean that the only data left to store per record
would be the key, which is 64 bits. That means that we would be able to sort
up to 2 billion records, or a csv file around 400GB in storage.

# Project Template

This is a Java Maven Project Template


# How to compile the project

We use Apache Maven to compile and run this project. 

You need to install Apache Maven (https://maven.apache.org/)  on your system. 

Type on the command line: 

```bash
mvn clean compile
```
# How to create a binary runnable package

```bash
mvn clean compile assembly:single
```

# AI usage

Because the policy is unclear (the Syllabus has all 3 policies it seems), we feel the
need to disclose how we used AI in the implementation of this project.

In terms of writing code, we used it to generate equivalents to functions we had already
written. For example, after we write StringSerialization.Cursor and the parseLine function,
we used AI to generate the equivalent toString() function that did the reverse. We also
used AI to generate TaxiDataPool#read after we had written TaxiDataPool#addTaxiAndGetIndex.

In terms of logic, we consulted with AI to check if our logical designs were correct or not,
and to spot bugs after we had written the code.
### Auto-completion example model of search engine
    Raw data ----> Preprocessing or Machine Learning ----> Database ----> Web Interface <br>
                       |
                       |-------> MapReduce Job to process raw data offline
                                       |
                                       |-----> MySQL   <input_phrase    predicted_phrase    corrsponding_probability>
                                       
#### What is N-Gram Model?

1. N-Gram: A n-gram is a contiguous sequence of n items from a given sequence
           of text or speech<br>
    example:<br>
    A B C D<br>
    4-Gram: A B C D<br>
    3-Gram: A B C, B C D<br>

##### What is Language Model?
    A language model is a probability distribution over entire sentences or texts

I want to eat _____:<br>
    A. apple `P(apple) > P(knife)` <br>
    B. knife <br>

    So N-Gram Model is to use probability to predict next word / phrase


Predict N-Gram based on 1-Gram: <br>
    I love eating (apple/knife/shit ...) --->  the prediction of () is only based on the previous one gram

Predict N-Gram based on N-Gram <br>
    I love eating (apple/knife/shit ...) ---> the prediction of () is based on n previous grams <br>
    
                                       
#### What data to store in database
    <input_phrase    predicted_phrase    corresponding_probability>   three columns to store
                                         probability can also be used to determine how many predictions shown on web
                                         

#### Implement N-Gram Model
    Database    <----                MapReduce Job2    <----          MapReducer Job1
                                   calculate probability            calculate total count for n gram
                                   write to the database 
                        
    hello | world | 100            Mapper: hello word | 100             Mapper: hello world | 1, 1, 1 .... (100 times)   
                       
                                   Reducer: hello | world | 100         Reducer: hello world | 100
                                   
                                   
    Steps:
        1. Read a large-scale document collections
                
        2. Build n-gram library (First MapReduce Job)
               |
               |
               |--------> 2-gram                 3-gram                ....     n-gram
                          want to   | 200
                          eat apple | 120
                          eat knife | 1
               
        3. Calculate probability (Second MapReduce Job) and write result into Database
               |
               |
               |-------> want | to = 200  calculate P(to | want) = ???
                         
                         eat  | apple = 120   calculate P(apple | eat) = ???
                                knife = 1
                        
        4. Run the project on MapReduce
   
##### Document Preprocessing:
* Read each document
    * sentence by sentence<br>
        HDFS reads documents line by line by defaut<br>
        However we need to obtain the context of documents so<br> 
        we set HDFS read document sentence by sentence<br>
* Remove all non-alphabetical symbols

##### Build N-Gram Library
![N-Gram Library](N-Gram%20Library.png)

* Build N-Gram Library by MapReduce
    ```
    Word Count is 1-gram impelemented by MapReduce
    
    More generally:
        N-Gram:
        
    Today is so cold since it is snowing outside
                 | first time split
                \ /
    Today is, is so, so cold, cold since, since it, it is, is snowing, snowing outside
                 |
                \ /
    
    ```
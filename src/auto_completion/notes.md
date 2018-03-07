#### What is N-Gram Model?

1. N-Gram: A n-gram is a contiguous sequence of n items from a given sequence
           of text or speech<br>
    example:<br>
    A B C D<br>
    4-Gram: A B C D<br>
    3-Gram: A B C, B C D<br>

##### What is Language Model?
    A language model is a probability distribution over entire sentences or texts

I want to eat _____:
    A. apple          P(apple) > P(knife) <br>
    B. knife <br>

So N-Gram Model is to use probability to predict next word / phrase


Predict N-Gram based on 1-Gram: <br>
    I love eating (apple/knife/shit ...) --->  the prediction of () is only based on the previous one gram

Predict N-Gram based on N-Gram <br>
    I love eating (apple/knife/shit ...) ---> the prediction of () is based on n previous grams
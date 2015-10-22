
###[Machine Learning---LMS 算法](http://blog.csdn.net/Stan1989/article/details/8589079)
分类： Machine Learning 2013-02-18 20:31 2262人阅读 评论(7) 收藏 举报
Least Mean SquaresLMS算法人工智能

目录(?)[+]
Machine Learning---LMS 算法
引言

简单的感知器学习算法（《Machine Learning---感知器学习算法》）会将真个集合正确分类后，才会停止，显然当测试数据多的时候，这种算法会变得迟钝。所以这里，引入一个理念，最小均方算法（Least Mean Square）。
一、LMS算法基本介绍
1.历史

LMS算法首先由Bernard Widrow和Marcian E. Hoff提出，被用于分类计算。大大降低了分类算法的复杂度。LMS算法是一种梯度下降法(Gradient Descent)。

对于LMS的数学证明，这里暂时不做介绍。

所以下面提到的公式，也只做简单性说明，请见谅。
2.均方差

均方差（Mean Square Error）这个概念我就用下面这个公式进行介绍。

 公式（1）

上面的公式1中的R表示正确的预期结果，C表示当前计算结果。这个便是LMS算法中终止算法的核心公式。

对于如何得到“当前计算结果C”，按照下面这个公式进行计算

   公式（2）

对于该公式，笔者在《Machine Learning---感知器学习算法》中有介绍。这里就只做简单解释：i表示输入值，W表示输入端所对应的权值，对这两个值进行乘法运算后，并求和。对于求和的结果可以进行一定处理，比如大于0的O便为1；否则就为-1。
3.权值调整公式

用于调整输入端的权值。

 公式（3）

在算法运行时，不断利用公式2进行输入端的权值调整，使权值越来越接近正确值。其中w便是输入端所对应的权值，I便是输入值， 便是学习参数，一般为小于1的正数。
4.算法流程

下面介绍一下LMS算法的基本流程。

1.     初始化工作，为各个输入端的权值覆上随机初始值；

2.     随机挑选一组训练数据，进行计算得出计算结构C；

3.     利用公式3对每一个输入端的权值进行调整；

4.     利用公式1计算出均方差MSE；

5.     对均方差进行判断，如果大于某一个给定值，回到步骤2，继续算法；如果小于给定值，就输出正确权值，并结束算法。
二、算法实现

以下就给出一段LMS算法的代码。

[cpp] view plaincopyprint?

    const unsigned int nTests   =4;  
    const unsigned int nInputs  =2;  
    const double rho =0.005;  
       
    struct lms_testdata  
    {  
        doubleinputs[nInputs];  
        doubleoutput;  
    };  
       
    double compute_output(constdouble * inputs,double* weights)  
    {  
        double sum =0.0;  
        for (int i = 0 ; i < nInputs; ++i)  
        {  
            sum += weights[i]*inputs[i];  
        }  
        //bias  
        sum += weights[nInputs]*1.0;  
        return sum;  
    }  
    //计算均方差  
    double caculate_mse(constlms_testdata * testdata,double * weights)  
    {  
        double sum =0.0;  
        for (int i = 0 ; i < nTests ; ++i)  
        {  
            sum += pow(testdata[i].output -compute_output(testdata[i].inputs,weights),2);  
        }  
        return sum/(double)nTests;  
    }  
    //对计算所得值,进行分类  
    int classify_output(doubleoutput)  
    {  
        if(output> 0.0)  
            return1;  
        else  
            return-1;  
    }  
    int _tmain(int argc,_TCHAR* argv[])  
    {  
        lms_testdata testdata[nTests] = {  
            {-1.0,-1.0, -1.0},  
            {-1.0, 1.0, -1.0},  
            { 1.0,-1.0, -1.0},  
            { 1.0, 1.0,  1.0}  
        };  
        doubleweights[nInputs + 1] = {0.0};  
        while(caculate_mse(testdata,weights)> 0.26)//计算均方差,如果大于给定值,算法继续  
        {  
            intiTest = rand()%nTests;//随机选择一组数据  
            doubleoutput = compute_output(testdata[iTest].inputs,weights);  
            doubleerr = testdata[iTest].output - output;  
            //调整输入端的权值  
            for (int i = 0 ; i < nInputs ; ++i)  
            {  
                weights[i] = weights[i] + rho * err* testdata[iTest].inputs[i];  
            }  
            weights[nInputs] = weights[nInputs] +rho * err;  
            cout<<"mse:"<<caculate_mse(testdata,weights)<<endl;  
        }  
       
        for(int w = 0 ; w < nInputs + 1 ; ++w)  
        {  
            cout<<"weight"<<w<<":"<<weights[w]<<endl;  
        }  
        cout<<"\n";  
        for (int i = 0 ;i < nTests ; ++i)  
        {  
            cout<<"rightresult：êo"<<testdata[i].output<<"\t";  
            cout<<"caculateresult:" << classify_output(compute_output(testdata[i].inputs,weights))<<endl;  
        }  
        //  
        char temp ;  
        cin>>temp;  
        return 0;  
    }  

三、总结

LMS算法的数学方面的说明比较麻烦,所以笔者想之后单独写一篇。

如果有兴趣的可以去看维基百科关于LMS算法的说明，这篇暂时只做编程上的简单介绍。


由于笔者不是专门研究人工智能方面，所以在写这些文章的时候，肯定会有一些错误，也请谅解，上面介绍中有什么错误或者不当地方，敬请指出，不甚欢迎。

如果有兴趣的可以留言，一起交流一下算法学习的心得。


声明：本文章是笔者整理资料所得原创文章，如转载需注明出处，谢谢。



Machine Learning---感知器学习算法
分类： Machine Learning 2013-02-02 16:40 3382人阅读 评论(1) 收藏 举报
感知器学习算法监督式学习神经网络

目录(?)[+]
Machine Learning---感知器学习算法
引言

这里开始介绍神经网络方面的知识（Neural Networks）。首先我们会介绍几个监督式学习的算法，随后便是非监督式的学习。
一、感知器学习算法基本介绍

1.神经网络

就像进化计算，神经网络又是一个类似的概念。神经网络由一个或者多个神经元组成。而一个神经元包括输入、输出和“内部处理器”。神经元从输入端接受信息，通过“内部处理器”将这些信息进行一定的处理，最后通过输出端输出。
2.感知器

感知器（Perceptron），是神经网络中的一个概念，在1950s由Frank Rosenblatt第一次引入。
3.单层感知器

单层感知器（Single Layer Perceptron）是最简单的神经网络。它包含输入层和输出层，而输入层和输出层是直接相连的。


图1.1

图1.1便是一个单层感知器，很简单一个结构，输入层和输出层直接相连。

接下来介绍一下如何计算输出端。


利用公式1计算输出层，这个公式也是很好理解。首先计算输入层中，每一个输入端和其上的权值相乘，然后将这些乘机相加得到乘机和。对于这个乘机和做如下处理，如果乘机和大于临界值（一般是0），输入端就取1；如果小于临界值，就取-1。

以下就给出一段单层感知器的代码。

[cpp] view plaincopyprint?

    //////////////////////////////////////////////////////////////////////////  
    //singlelayer perceptrons(SLP)  
    bool slp_calculate_output(constdouble * inputs,constdouble * weights,intnInputs,int & output)  
    {  
        if(NULL ==inputs || NULL == weights)  
            return false;  
        double sum =0.0;  
        for (int i = 0 ; i < nInputs ; ++i)  
        {  
            sum += (weights[i] * inputs[i]);  
        }  
    //这里我们对乘机和的处理：如果大于0，则输出值为1；其他情况，输出值为-1  
        if(sum >0.0)  
            output = 1;  
        else  
            output = -1;  
    }  
    //////////////////////////////////////////////////////////////////////////  

单层感知器其简单的特性，可以提供快速的计算。它能够实现逻辑计算中的NOT、OR、AND等简单计算。

但是对于稍微复杂的异或就无能无力。下面介绍的多层感知器，就能解决这个问题。
4.多层感知器

多层感知器（Multi-Layer Perceptrons），包含多层计算。

相对于单层感知器，输出端从一个变到了多个；输入端和输出端之间也不光只有一层，现在又两层:输出层和隐藏层。


图2.2

图2.2就是一个多层感知器。

对于多层感知器的计算也是比较简单易懂的。首先利用公式1计算每一个。

看一下它代码，就能明白它的工作原理。

[cpp] view plaincopyprint?

    //////////////////////////////////////////////////////////////////////////  
    //Multi-Layerperceptrons(MLP)  
    const unsignedint nInputs  =4;  
    const unsignedint nOutputs = 3;  
    const unsignedint nHiddens = 4;  
    struct mlp  
    {  
        doubleinputs[nInputs+1];//多一个，存放的bias，一般存放入1  
        doubleoutputs[nOutputs];  
        doublehiddens[nHiddens+1]; //多一个，存放的bias，一般存放入1  
        doubleweight_hiddens_2_inputs[nHiddens+1][nInputs+1];  
        doubleweight_outputs_2_hiddens[nOutputs][nHiddens+1];  
    };  
    //这里我们对乘机和的处理：如果大于0，则输出值为1；其他情况，输出值为-1  
    double sigmoid (double val)  
    {  
        if(val >0.0)  
            return1.0;  
        else  
            return-1.0;  
    }  
    //计算输出端  
    bool mlp_calculate_outputs(mlp * pMlp)  
    {  
        if(NULL ==pMlp)  
            return false;  
        double sum =0.0;  
        //首先计算隐藏层中的每一个结点的值  
        for (int h = 0 ; h < nHiddens ; ++h)  
        {  
            doublesum = 0.0;  
            for (int i = 0 ; i < nInputs + 1 ; ++i)  
            {  
                sum += pMlp->weight_hiddens_2_inputs[h][i]*pMlp->inputs[i];  
            }  
           pMlp->hiddens[h] = sigmoid (sum);  
       
        }  
         //利用隐藏层作为“输入层”，计算输出层  
        for (int o = 0 ; o < nOutputs ; ++o)  
        {  
            doublesum = 0.0;  
            for (int h = 0 ; h < nHiddens + 1 ; ++h)  
            {  
                sum += pMlp->weight_outputs_2_hiddens[o][h]*pMlp->hiddens[h];  
            }  
            pMlp->outputs[o] = sigmoid (sum);  
        }  
        return true;  
    }  
    //////////////////////////////////////////////////////////////////////////  



二、感知器学习算法

1.感知器学习

其实感知器学习算法，就是利用第一节介绍的单层感知器。首先利用给的正确数据，计算得到输出值，将输出值和正确的值相比，由此来调整每一个输出端上的权值。


公式2便是用来调整权值，首先 是一个“学习参数”，一般我将它设置成小于1的正数。T便是训练数据中的正确结果， 便是第i个输入端的输入值，便是第i个输入端上面的权值。
2.代码

对于其介绍，我还是附上代码。

[cpp] view plaincopyprint?

    //////////////////////////////////////////////////////////////////////////  
    //PerceptronLearning Algorithm(PLA)  
    const unsignedint nTests   =4; //训练数据的数量  
    const unsignedint nInputs  =2; //输入端的数量  
    const double alpha =0.2;       //学习参数  
    struct slp  
    {  
        doubleinputs[nInputs];  
        doubleoutput;  
    }; //单层感知器  
    //计算输出值  
    int compute(double *inputs,double * weights)  
    {  
        double sum =0.0;  
        for (int i = 0 ; i < nInputs; ++i)  
        {  
            sum += weights[i]*inputs[i];  
        }  
        //bias  
        sum += 1.0 * weights[nInputs];  
        if(sum >0.0)  
            return1;  
        else  
            return-1;  
    }  
    //  
    int _tmain(int argc,_TCHAR* argv[])  
    {  
    //正确的训练数据  
        slp slps[nTests] = {  
            {-1.0,-1.0,-1.0},  
            {-1.0, 1.0, 1.0},  
            { 1.0,-1.0, 1.0},  
            { 1.0, 1.0, 1.0}  
        };  
        doubleweights[nInputs + 1] = {0.0};  
        boolbLearningOK = false;  
      //感知器学习算法  
        while(!bLearningOK)  
        {  
            bLearningOK = true;  
            for (int i = 0 ; i < nTests ; ++i)  
            {  
                intoutput = compute(slps[i].inputs,weights);  
                if(output!= (int)slps[i].output)  
                {  
                    for(int w = 0 ; w < nInputs ; ++w)  
                    {  
                        weights[w] += alpha *slps[i].output * slps[i].inputs[w];  
                    }  
                    weights[nInputs] += alpha *slps[i].output ;  
                    bLearningOK = false;  
                }  
            }  
        }  
        for(int w = 0 ; w < nInputs + 1 ; ++w)  
        {  
            cout<<"weight"<<w<<":"<<weights[w] <<endl;  
        }  
        cout<<"\n";  
        for (int i = 0 ;i < nTests ; ++i)  
        {  
            cout<<"rightresult："<<slps[i].output<<"\t";  
            cout<<"caculateresult:" << compute(slps[i].inputs,weights)<<endl;  
        }  
         
        //  
        char temp ;  
        cin>>temp;  
        return 0;  
    }  

2.效果图

下面附上运行效果图


三、总结

感知器学习算法，算是神经网络中的最简单的学习算法。但是通过这个进入学习神经网络学习算法，是个不错的选择。

感知器学习算法，只要是利用了单层感知器。这篇文章中，我们还了解到了另一种感知器：多层感知器。多层感知器主要是用于方向传播学习算法中，这个我后面的文章中会进行介绍。

由于笔者不是专门研究人工智能方面，所以在写这些文章的时候，肯定会有一些错误，也请谅解，上面介绍中有什么错误或者不当地方，敬请指出，不甚欢迎。

 

如果有兴趣的可以留言，一起交流一下算法学习的心得。

声明：本文章是笔者整理资料所得原创文章，如转载需注明出处，谢谢。


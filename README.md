<img width="125" src="/assets/img/tetris/tetris.png">

### Introduction 
The AI Tetris Agent essentially is a computer program that plays the game [Tetris](https://en.wikipedia.org/wiki/Tetris) without any human aid. The goal of the program is to simply clear as many lines as possible - in this case, the agent clears 5 million lines per game on average.      
        
### Usage 
AI Tetris Agent is available for the Linux, Windows, and Mac OS operating systems.        
        
1. Ensure you have Java installed in your computer      
2. Ensure that you have Java Development Kit (JDK) installed on your computer      
3. Clone the git repository from [here](https://github.com/jaivigneshvenugopal/Tetris-Agent)      
4. Run the following command in the terminal: `javac PlayerSkeleton.java` 
5. Followed by this: `java PlayerSkeleton`      
   
 The agent in progress:

<img width="1080" src="/assets/img/tetris/tetris1.gif">      
 
### Understanding the Problem   
 The rules of the game are fairly straightforward. A piece slowly descends from the top and the player is supposed to tactically place it in a position such that it aligns perfectly with the other resting pieces - block of lines or a singular line disappears every time this happens and points are earned. The player is allowed to rotate the falling pieces to his liking before proceeding to place it.      
  
<img src="/assets/img/tetris/tetris2.png">

[Picture [credits](https://ntnuopen.ntnu.no/ntnu-xmlui/bitstream/handle/11250/253727/751065_FULLTEXT01.pdf?sequence=2&isAllowed=y)]  
  
For an AI to perform this task similarly - and well - it has to firstly know in which orientation it should place the piece and precisely in what column its final destination should be. Neither the human or the AI could look into the future to determine what the next piece will be. One can only decide on what to do with the current piece at the moment it appears. As long as this is done well, the game could be beaten with a very high score (in the range of millions).  
      
However, it is difficult to define what it means to play a good move. Humans are able to intuitively know if the current state of the game conveys great potential for a long game, whereas it is difficult for the same human to explicitly program this knowledge to define a good move.     
    
#### Heuristics
This brings forth a need for heuristics. Heuristics of a game state (the game at a particular point in time) are basically insights/estimations that could be used to determine if the current state of the game would lead to a high score.     
    
These were some of the heuristics used in building the agent:      
      
1. **Number of Holes** - Number of rows having at least one hole  
2. **Holes Depth** - Number of full cells in the column above each hole   
3. **Cumulative Wells** -  Sum of the depth of the wells (differs from holes)  
4. **Cell Transitions** - The number of empty cells/borders touching the edges of full cells       
5. **Eroded Piece Cells**   

<img src="/assets/img/tetris/tetris3.png">  

[**Wells** and **Holes** - Picture [credits](https://ntnuopen.ntnu.no/ntnu-xmlui/bitstream/handle/11250/253727/751065_FULLTEXT01.pdf?sequence=2&isAllowed=y)]  
  
The idea is that once the agent learns what the next piece is, it runs simulations for all possible moves - orientation and position - using the incoming piece. After simulating each move, it uses the heuristics on the new game state to decide on the most rewarding move among all possible moves. This way, it will have enough time to rotate and place it before time runs out. Doing so sequentially for every single new piece would guarantee a good score in the long run.    
      
However, the problem is to decide on how much weightage should each heuristic get. The agent technically could use a single heuristic instead of multitude of the aforementioned ones. However, this usually does not yield the best results, resulting in the need for multiple heuristics. The root problem, however, is finding the best relative weights for the heuristics.    
      
The problem of beating Tetris using AI essentially translates to an optimization problem - finding the right weights for the heuristics.    
    
#### Optimization Problem 
An optimization problem calls for an optimization algorithm. There are many such algorithms available to solve this problem. A few to name are Artificial Bee Colony (ABC) algorithm, Genetic algorithm, and Particle Swarm Optimization algorithm.     
    
This was a project done with 4 other students in an AI class. Therefore, there was a need to research about the feasibility of different algorithms. A fellow student and I delved into an algorithm called Cuckoo's Algorithm while the rest worked on others. Eventually we went ahead with ABC algorithm since it was shorter and easier to explain. However, the following paragraphs will explain the Cuckoo's algorithm since it performed similarly to the ABC algorithm and also since I worked extensively on it.    
      
### The Solution: Cuckoo's Algorithm 

<img width="400" src="/assets/img/tetris/tetris4.jpg">  

[Picture [credits](https://en.wikipedia.org/wiki/Cuckoo)]
  
The paper describing this algorithm could be found [here](https://arxiv.org/pdf/1003.1594.pdf).    
  
Cuckoos usually lay eggs in other bird's nests to increase survival and productivity of its own. They do this by imitating the color and pattern of host eggs. Cuckoo's eggs will hatch earlier than host's eggs, allowing cuckoo chick/s to evict all other eggs in nest to increase food share. 

The **algorithm** follows a similar strategy: 

Each nest represents a solution - normalized weights for each heuristic - and the number of nests are usually initialized to 15. Initially all the nests are initialized randomly, however, each of the solution's potential is gauged by running simulations. 

After an iteration, a percentage (20-30%) of the nests are randomly discarded. To pad the removed solutions, new solutions are generated by using the remaining solutions and doing [Levy](https://en.wikipedia.org/wiki/L%C3%A9vy_flight) walks with them. 

This process is repeated till a good enough solution emerges.

This algorithm eventually - after running for 7/8 hours or so - yielded a solution that cleared 5 millions lines on average.

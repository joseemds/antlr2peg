= Transformar uma repetição p*, p+, p?

1. FIRST(p) \cap FOLLOW(p*) \= vazio
  - Substitui p* por nova regra A 
  - A -> p A / &FOLLOW(p*)
  - Desempenho: considerar só a interseção INTER = FIRST(p) \cap FOLLOW(p*)

  
2. FIRST(p) \cap FOLLOW(p*) == vazio
  - Mantém p*

3. FIRST(p) \cap FOLLOW(p+) \= vazio
  - Substitui p+ por nova regra A
  - A -> p A / &(p FOLLOW(p+)) p 
  - Desempenho:
    + p+ = p p*
    + Substituiu p+ por p A, onde A é uma nova regra
    + A -> p A / &FOLLOW(p+)
    
    
4. FIRST(p) \cap FOLLOW(p+) == vazio
  - Mantém p+

5. FIRST(p) \cap FOLLOW(p?) \= vazio
  - Substitui p? por nova regra A
  - A -> p &FOLLOW(p?) / ''
  
6. FIRST(p) \cap FOLLOW(p?) == vazio
  - Mantém p?


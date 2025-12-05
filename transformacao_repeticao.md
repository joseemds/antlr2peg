= Transformar uma repetição p*, p+, p?

1. FIRST(p) \cap FOLLOW(p*) \= vazio
  - Substitui p* por nova regra A 
  - A -> p A / &FOLLOW(p*)
  
2. FIRST(p) \cap FOLLOW(p*) == vazio
  - Mantém p*

3. FIRST(p) \cap FOLLOW(p+) \= vazio
  - Substitui p+ por nova regra A
  - A -> p A / &(p FOLLOW(p+))
  
4. FIRST(p) \cap FOLLOW(p+) == vazio
  - Mantém p+

5. FIRST(p) \cap FOLLOW(p?) \= vazio
  - Substitui p? por nova regra A
  - A -> p &FOLLOW(p?) / ''
  
6. FIRST(p) \cap FOLLOW(p?) == vazio
  - Mantém p?


#### 01-06 Julho 2025

- Suporte a seed na geração de problemas
- Implementado Gramatica ABNF 
- Possível Problema (?): match de "%" tanto em `STRING` quanto em  `NumberRule`
- Problema: Identificar quando uma "rule" termina

```
mA = / %s"-" 
p = "test"    / %xcc    / [ *  %xF-C    / 4 * 12486  ""     ]    %xb       
```

- Solução: Predicado em element `ID` para verificar se não vem antes de `tk"="`
- Melhorado makefile para portabilidade
- TODO: Atribuir pesos aos geradores

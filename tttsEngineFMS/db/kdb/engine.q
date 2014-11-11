trade:([]time:`time$();sym:`symbol$();price:`float$();size:`int$())

`trade insert(09:30:00.000;`a;10.75;100)

select sum size by sym from trade

engine:([]funds:`float$();balance:`float$();transnum:`long$();intrade:`boolean$();possize:`long$())

`engine insert(5000.00;5000.00;0;0b;0)



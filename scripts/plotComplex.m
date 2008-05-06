filename='C:\\MAStools\\workspace\\FinancialModel\\timeSeries.txt';
[h, run]=hdrload(filename);

index=6;
lag=10;


data=zeros(length(run)-lag+1,2);

data(:,1)=run(lag:length(run),index);
data(:,2)=run(1:(length(run)-lag+1),index);

ccolors = vector2colors(run(1:(length(run)-lag+1),2));
scatter(data(:,1),data(:,2),5,ccolors,'filled'); 
    

hold on;

for i=2:length(run)
    
end

hold off;
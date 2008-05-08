filename='..\\timeSeries.txt';
[h, allRuns]=hdrload(filename);

maxRuns=100;

hursts=zeros(maxRuns,1);

hold all;

for run=1:maxRuns
    run
    data = allRuns(find((allRuns(:,1)==run) & (allRuns(:,2)>2000)),6);
    hursts(run) = estimate_hurst_exponent(data');
end

title('ACF for different values of D','FontSize',14);
xlabel('Lag','FontSize',14);
ylabel('Correlatoin','FontSize',14);
legend('1','0.1','0.01','0.001','0.0001','0.00001');

hold off

 indicies=find(vec(:,7)==size);
    fitx=(1+abs(vec(indicies,5)));
    fity=(1+abs(vec(indicies,6)));

    r=ksr(fitx,fity,1,100);

    mins=10000000+zeros(length(r.x),1);
    maxs=zeros(length(r.x),1);

    for c=1:20
        th=rand(length(fitx),1);
        indicies=find(th(:)>0.95);
        tfitx=fitx(indicies);
        tfity=fity(indicies);
        r=ksr(tfitx,tfity,1,100);
        mins=min(mins,r.f');
        maxs=max(maxs,r.f');
    end

    r=ksr(fitx,fity,1,100);

    plot(r.x,r.f,'LineWidth',1,'Color',char(colors(size)));
    ciplot(mins,maxs,r.x,char(colors(size)));

    size

filename = '..\\graphics\\comparisonOfACFs.pdf';
orient tall;
print('-dpdf', filename);
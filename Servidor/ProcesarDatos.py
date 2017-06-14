import pandas as pd
import argparse

from scipy.fftpack import fft as fft_sc

import numpy as np
from datetime import datetime
import math
from numpy.fft import fft, ifft, rfft, irfft
from numpy import sqrt, mean, absolute, real, conj

from scipy.stats import skew, kurtosis

def corr(df):
    cor=df.corr()
    return pd.DataFrame({'xy': [cor['accel-x']['accel-y']],'xz':[cor['accel-x']['accel-z']], 'yz':[cor['accel-y']['accel-z']]})
def tfft(df):
     X=fft(df['accel-x'])
     Y=fft(df['accel-y'])
     Z=fft(df['accel-z'])  
     return pd.DataFrame({'x': [sum(abs(X)**2)/len(df)],'y': [sum(abs(Y)**2)/len(df)], 'z':[sum(abs(Z)**2)/len(df)]})
     
def tfft_scipy(df):
     X=fft_sc(df['accel-x'])
     Y=fft_sc(df['accel-y'])
     Z=fft_sc(df['accel-z'])  
     return pd.DataFrame({'x': [sum(abs(X)**2)/len(df)],'y': [sum(abs(Y)**2)/len(df)], 'z':[sum(abs(Z)**2)/len(df)]})
     
def skew_sc(df):
     X=skew(df['accel-x'])
     Y=skew(df['accel-y'])
     Z=skew(df['accel-z'])  
     return pd.DataFrame({'x': [X],'y': [Y], 'z':[Z]})
     
def kurtosis_sc(df):
     X=kurtosis(df['accel-x'])
     Y=kurtosis(df['accel-y'])
     Z=kurtosis(df['accel-z'])  
     return pd.DataFrame({'x': [X],'y': [Y], 'z':[Z]})

     
def tfft_suma(df):
     X=fft(df['accel-x'])
     Y=fft(df['accel-y'])
     Z=fft(df['accel-z'])  
     return pd.DataFrame({'x': [sum(abs(X))],'y': [sum(abs(Y))], 'z':[sum(abs(Z))]})
     
def tfft_std(df):
     X=fft(df['accel-x'])
     Y=fft(df['accel-y'])
     Z=fft(df['accel-z'])  
     return pd.DataFrame({'x': [np.std(abs(X))],'y': [np.std(abs(Y))], 'z':[np.std(abs(Z))]})
     
def tfft_media(df):
     X=fft(df['accel-x'])
     Y=fft(df['accel-y'])
     Z=fft(df['accel-z'])  
     return pd.DataFrame({'x': [np.mean(abs(X))],'y': [np.mean(abs(Y))], 'z':[np.mean(abs(Z))]})
    
    
def rms_flat(a):
  
    return sqrt(mean(absolute(a)**2))   
    
def rms_fft(df):
     X=fft(df['accel-x'])
     Y=fft(df['accel-y'])
     Z=fft(df['accel-z'])  
     return pd.DataFrame({'x': [rms_flat(X)/sqrt(len(X))],'y': [rms_flat(Y)/sqrt(len(Y))], 'z':[rms_flat(Z)/sqrt(len(Z))]})
     
def rms_rfft(spectrum, n=None):
    
   
    if n is None:
        n = (len(spectrum) - 1) * 2
    sq = real(spectrum * conj(spectrum))
    if n % 2:  # odd-length
        mean = (sq[0] + 2*sum(sq[1:])           )/n
    else:  # even-length
        mean = (sq[0] + 2*sum(sq[1:-1]) + sq[-1])/n
    root = sqrt(mean)
    return root/sqrt(n)
def rms_rfftdf(df):
      X=rfft(df['accel-x'])
      Y=rfft(df['accel-y'])
      Z=rfft(df['accel-z'])  
      
      x_rfft= rms_rfft(X, len(df))
      y_rfft= rms_rfft(Y, len(df))
      z_rfft= rms_rfft(Z, len(df))
      return pd.DataFrame({'x': [x_rfft],'y': [y_rfft], 'z':[z_rfft]})
  
   

    
def getStatisticsValues(nombre, numeroFicheros, ruta_timestamp, ruta_timestamp_tmp, time1=1, overlap=500):
    df_final = pd.DataFrame()
 
    
    for i in range(0, int (numeroFicheros)):
        df = pd.read_csv(ruta_timestamp + "%s_%d.csv" %(nombre, i+1), sep=',', index_col=0, error_bad_lines=False)
       
        df.index = pd.to_datetime(df.index.values, unit='ms')
        
       
        dfResampleMean = df.resample('%dL' %(overlap)).mean()
        dfRollingMean = dfResampleMean.rolling('%ds' %(time1)).mean().add_suffix("_avg")
        df_out=dfRollingMean

        dfResampleMin = df.resample('%dL' %(overlap)).min()
        dfRollingMin = dfResampleMin.rolling('%ds' %(time1)).min().add_suffix("_min")
        df_out=df_out.join(dfRollingMin)
        
        dfResampleMax = df.resample('%dL' %(overlap)).max()
        dfRollingMax = dfResampleMax.rolling('%ds' %(time1)).max().add_suffix("_max")
        df_out=df_out.join(dfRollingMax)
        
        
        dfResampleStd = df.resample('%dL' %(overlap)).std()
        dfRollingStd = dfResampleStd.rolling('%ds' %(time1)).std().add_suffix("_std")
        df_out=df_out.join(dfRollingStd)
       
        dfResampleCor=df.groupby(pd.TimeGrouper('%dL' %(overlap))).apply(corr).reset_index(1,drop=True).add_suffix("_cor")       
        df_out=df_out.join(dfResampleCor)
       
        
        dfResamplefft=df.groupby(pd.TimeGrouper('%dL' %(overlap))).apply(tfft).reset_index(1,drop=True).add_suffix("_fft")
        df_out=df_out.join(dfResamplefft)
           
        
        dfResampleMed = df.resample('%dL' %(overlap)).median()
        dfRollingMed = dfResampleMed.rolling('%ds' %(time1)).median().add_suffix("_med")
        df_out=df_out.join(dfRollingMed)
      
       
        df_final=df_final.append(df_out)
     
    df_final=df_final.fillna(df_final.mean())
    fecha = datetime.now().microsecond
   
    df_final.to_csv(ruta_timestamp_tmp + "%s_procesado_%s.csv" %(nombre, fecha), ';', header=True, index=True)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Extract features from inputFile and save them in outputFile')

    parser.add_argument("i",
                        help="File/s to be analyzed")
    parser.add_argument("n",
                        help="Number of files")
    parser.add_argument("-t", "--time", help="Time of window, i.e.= 1 second",
                    default=1)
    parser.add_argument("-o", "--overlap", help="overlap || 500ms -> 50.00perc verlap",
                    default=500)
    args = parser.parse_args()

    getStatisticsValues(args.i, args.n, args.time, args.overlap)

package com.nowconder.community;

public class test0319 {
    public static void main(String[] args) {
        int[] arr1 = {1,2,3,4,5};
        int[] arr2 = {1,2,3,4};

        int[] arr3 = {3,2,4,1,5};
        int[] arr4 = {2,3,1,4};

        int[] ans = decode(arr4);
        for(int num:ans){
            System.out.println(num);
        }

    }
    public static int[] encode(int[] arr){
        int n = arr.length;
        int[] ans = new int[n];
//        int index = n/2;
        if(n%2==0){
            double index = n/2-1;
            for(int i=0;i<n;i++){
                index = index + Math.pow(-1,i+1) * i;
                ans[i] = arr[(int)index];
            }
        }
        else{
            double index = n/2;
            for(int i=0;i<n;i++){
                index = index + Math.pow(-1,i) * i;
                ans[i] = arr[(int)index];
            }
        }
        return ans;
    }

    public static int[] decode(int[] arr){
        int n = arr.length;
        int[] ans = new int[n];
        if(n%2==0){
            double index = n/2-1;
            for(int i=0;i<n;i++){
                index = index + Math.pow(-1,i+1) * i;
                ans[(int)index] = arr[i];
            }
        }
        else{
            double index = n/2;
            for(int i=0;i<n;i++){
                index = index + Math.pow(-1,i) * i;
                ans[(int)index] = arr[i];
            }
        }
        return ans;
    }
}

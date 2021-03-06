import { NgModule } from '@angular/core';

import { NgxUploaderModule } from 'ngx-uploader';
import { CommonModule } from '@angular/common';
import { BrowserModule } from '@angular/platform-browser';
import { FileUploadComponent } from './file-upload.component';
import { FileSizePipe } from './filesize.pipe';

@NgModule({
    imports: [NgxUploaderModule, BrowserModule, CommonModule],
    exports: [FileUploadComponent, FileSizePipe],
    declarations: [FileUploadComponent, FileSizePipe],
    entryComponents: [FileUploadComponent]
})
export class AppFileUploaderModule {}

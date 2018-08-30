
#include <err.h>
#include <unistd.h>
#include <fcntl.h>
#include <android/log.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "bzip2/crctable.c"
#include "bzip2/bzlib.c"
#include "bzip2/compress.c"
#include "bzip2/decompress.c"
#include "bzip2/randtable.c"
#include "bzip2/blocksort.c"
#include "bzip2/huffman.c"
#include "cn_jcyh_peephole_utils_PatchUtil.h"


static off_t offtin(u_char *buf) {
	off_t y;

	y = buf[7] & 0x7F;
	y = y * 256;
	y += buf[6];
	y = y * 256;
	y += buf[5];
	y = y * 256;
	y += buf[4];
	y = y * 256;
	y += buf[3];
	y = y * 256;
	y += buf[2];
	y = y * 256;
	y += buf[1];
	y = y * 256;
	y += buf[0];

	if (buf[7] & 0x80)
		y = -y;

	return y;
}

int applypatch(int argc, char * argv[]) {
	FILE * f, *cpf, *dpf, *epf;
	BZFILE * cpfbz2, *dpfbz2, *epfbz2;
	int cbz2err, dbz2err, ebz2err;
	int fd;
	ssize_t oldsize, newsize;
	ssize_t bzctrllen, bzdatalen;
	u_char header[32], buf[8];
	u_char *old, *newPath;
	off_t oldpos, newpos;
	off_t ctrl[3];
	off_t lenread;
	off_t i;

	if (argc != 4)
		errx(1, "usage: %s oldfile newfile patchfile\n", argv[0]);

	if ((f = fopen(argv[3], "r")) == NULL)
		err(1, "fopen(%s)", argv[3]);

	if (fread(header, 1, 32, f) < 32) {
		if (feof(f))
			errx(1, "Corrupt patch\n");
		err(1, "fread(%s)", argv[3]);
	}

	if (memcmp(header, "BSDIFF40", 8) != 0)
		errx(1, "Corrupt patch\n");

	bzdatalen = offtin(header + 16);
	bzctrllen = offtin(header + 8);
	newsize = offtin(header + 24);
	if ((bzctrllen < 0) || (bzdatalen < 0) || (newsize < 0))
		errx(1, "Corrupt patch\n");

	if (fclose(f))
		err(1, "fclose(%s)", argv[3]);
	if ((cpf = fopen(argv[3], "r")) == NULL)
		err(1, "fopen(%s)", argv[3]);
	if (fseeko(cpf, 32, SEEK_SET))
		err(1, "fseeko(%s, %lld)", argv[3], (long long) 32);
	if ((cpfbz2 = BZ2_bzReadOpen(&cbz2err, cpf, 0, 0, NULL, 0)) == NULL)
		errx(1, "BZ2_bzReadOpen, bz2err = %d", cbz2err);
	if ((dpf = fopen(argv[3], "r")) == NULL)
		err(1, "fopen(%s)", argv[3]);
	if (fseeko(dpf, 32 + bzctrllen, SEEK_SET))
		err(1, "fseeko(%s, %lld)", argv[3], (long long) (32 + bzctrllen));
	if ((dpfbz2 = BZ2_bzReadOpen(&dbz2err, dpf, 0, 0, NULL, 0)) == NULL)
		errx(1, "BZ2_bzReadOpen, bz2err = %d", dbz2err);
	if ((epf = fopen(argv[3], "r")) == NULL)
		err(1, "fopen(%s)", argv[3]);
	if (fseeko(epf, 32 + bzctrllen + bzdatalen, SEEK_SET))
		err(1, "fseeko(%s, %lld)", argv[3],
				(long long) (32 + bzctrllen + bzdatalen));
	if ((epfbz2 = BZ2_bzReadOpen(&ebz2err, epf, 0, 0, NULL, 0)) == NULL)
		errx(1, "BZ2_bzReadOpen, bz2err = %d", ebz2err);

	if (((fd = open(argv[1], O_RDONLY, 0)) < 0)
		|| ((oldsize = lseek(fd, 0, SEEK_END)) == -1)
		|| ((old = malloc((size_t) (oldsize + 1))) == NULL)
		|| (lseek(fd, 0, SEEK_SET) != 0)
		|| (read(fd, old, (size_t) oldsize) != oldsize) || (close(fd) == -1))
		err(1, "%s", argv[1]);
	if ((newPath = malloc((size_t) (newsize + 1))) == NULL)
		err(1, NULL);

	oldpos = 0;
	newpos = 0;
	while (newpos < newsize) {
		for (i = 0; i <= 2; i++) {
			lenread = BZ2_bzRead(&cbz2err, cpfbz2, buf, 8);
			if ((lenread < 8)
					|| ((cbz2err != BZ_OK) && (cbz2err != BZ_STREAM_END)))
				errx(1, "Corrupt patch\n");
			ctrl[i] = offtin(buf);
		};

		if (newpos + ctrl[0] > newsize)
			errx(1, "Corrupt patch\n");

		lenread = BZ2_bzRead(&dbz2err, dpfbz2, newPath + newpos, ctrl[0]);
		if ((lenread < ctrl[0])
				|| ((dbz2err != BZ_OK) && (dbz2err != BZ_STREAM_END)))
			errx(1, "Corrupt patch\n");

		for (i = 0; i < ctrl[0]; i++)
			if ((oldpos + i >= 0) && (oldpos + i < oldsize))
				newPath[newpos + i] += old[oldpos + i];

		newpos += ctrl[0];
		oldpos += ctrl[0];

		if (newpos + ctrl[1] > newsize)
			errx(1, "Corrupt patch\n");

		lenread = BZ2_bzRead(&ebz2err, epfbz2, newPath + newpos, ctrl[1]);
		if ((lenread < ctrl[1])
				|| ((ebz2err != BZ_OK) && (ebz2err != BZ_STREAM_END)))
			errx(1, "Corrupt patch\n");

		newpos += ctrl[1];
		oldpos += ctrl[2];
	};

	BZ2_bzReadClose(&cbz2err, cpfbz2);
	BZ2_bzReadClose(&dbz2err, dpfbz2);
	BZ2_bzReadClose(&ebz2err, epfbz2);
	if (fclose(cpf) || fclose(dpf) || fclose(epf))
		err(1, "fclose(%s)", argv[3]);

	if (((fd = open(argv[2], O_CREAT | O_TRUNC | O_WRONLY, 0666)) < 0)
		|| (write(fd, newPath, (size_t) newsize) != newsize) || (close(fd) == -1))
		err(1, "%s", argv[2]);

	free(newPath);
	free(old);

	return 0;
}

JNIEXPORT void JNICALL Java_cn_jcyh_peephole_utils_PatchUtil_patch(JNIEnv *env,
																   jobject obj, jstring old, jstring newPath, jstring patch) {

	char* ch[4];
	ch[0] = "bspatch";
	ch[1] = (char*) ((*env)->GetStringUTFChars(env, old, 0));
	ch[2] = (char*) ((*env)->GetStringUTFChars(env, newPath, 0));
	ch[3] = (char*) ((*env)->GetStringUTFChars(env, patch, 0));




	__android_log_print(ANDROID_LOG_INFO, "ApkPatchLibrary", "old = %s ", ch[1]);
	__android_log_print(ANDROID_LOG_INFO, "ApkPatchLibrary", "newPath = %s ", ch[2]);
	__android_log_print(ANDROID_LOG_INFO, "ApkPatchLibrary", "patch = %s ", ch[3]);

	int ret = applypatch(4, ch);

	__android_log_print(ANDROID_LOG_INFO, "ApkPatchLibrary", "applypatch result = %d ", ret);

	(*env)->ReleaseStringUTFChars(env, old, ch[1]);
	(*env)->ReleaseStringUTFChars(env, newPath, ch[2]);
	(*env)->ReleaseStringUTFChars(env, patch, ch[3]);
	return ;
}








import 'package:firebase_ml_vision/firebase_ml_vision.dart';
import 'package:flutter/material.dart';
import 'package:flutter_camera_ml_vision/flutter_camera_ml_vision.dart';

class CameraPage extends StatefulWidget {
  CameraPage() : super();

  @override
  _CameraPageState createState() => _CameraPageState();
}

class _CameraPageState extends State<CameraPage> {
  List<String> data = [];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          RaisedButton(
            child: Text('Scan product'),
            onPressed: () async {
              final barcode = await Navigator.of(context).push<Barcode>(
                MaterialPageRoute(
                  builder: (c) {
                    return ScanPage();
                  },
                ),
              );
              if (barcode == null) {
                return;
              }

              setState(() {
                data.add(barcode.displayValue);
              });
            },
          ),
          Expanded(
            child: ListView(
              children: data.map((d) => Text(d)).toList(),
            ),
          ),
        ],
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}

class ScanPage extends StatefulWidget {
  @override
  _ScanPageState createState() => _ScanPageState();
}

class _ScanPageState extends State<ScanPage> {
  bool resultSent = false;
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: SizedBox(
          width: MediaQuery.of(context).size.width,
          child: CameraMlVision<List<Barcode>>(
            detector: FirebaseVision.instance.barcodeDetector().detectInImage,
            onResult: (List<Barcode> barcodes) {
              if (!mounted || resultSent) {
                return;
              }
              resultSent = true;
              Navigator.of(context).pop<Barcode>(barcodes.first);
            },
          ),
        ),
      ),
    );
  }
}
